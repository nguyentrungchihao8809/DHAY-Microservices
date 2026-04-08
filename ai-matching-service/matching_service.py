import logging
import datetime
import grpc
import asyncio
import threading # Để chạy vòng lặp event loop cho async trong thread gRPC
from concurrent import futures
import matching_pb2
import matching_pb2_grpc
from database import SessionLocal, PassengerRequestAI, DriverTripAI, init_db
import polyline
from app.tools.db_tool import find_nearby_passengers
from app.agents.graph import matching_agent

# Import Agent từ graph.py
from app.agents.graph import matching_agent

# --- Cấu hình Logging ---
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger("AI-Matching")

class MatchingService(matching_pb2_grpc.MatchingServiceServicer):

    def __init__(self):
        # Tạo một event loop riêng để xử lý các task async từ gRPC (Thread-safe)
        self.loop = asyncio.new_event_loop()
        thread = threading.Thread(target=self._run_event_loop, daemon=True)
        thread.start()

    def _run_event_loop(self):
        asyncio.set_event_loop(self.loop)
        self.loop.run_forever()

    # --- HÀM 1: ĐỒNG BỘ YÊU CẦU KHÁCH HÀNG (Giữ nguyên logic của bạn) ---
    def SyncPassengerRequest(self, request, context):
        db = SessionLocal()
        try:
            dt_str = request.departure_time.replace('Z', '+00:00')
            departure_dt = datetime.datetime.fromisoformat(dt_str)
            new_req = PassengerRequestAI(
                request_id=request.request_id,
                passenger_name=request.passenger_name,
                departure_time=departure_dt,
                seats_requested=request.seats_requested,
                start_geom=f'POINT({request.start_location.lng} {request.start_location.lat})',
                end_geom=f'POINT({request.end_location.lng} {request.end_location.lat})'
            )
            db.merge(new_req)
            db.commit()
            return matching_pb2.SyncResponse(success=True)
        except Exception as e:
            db.rollback()
            logger.error(f"❌ Lỗi SyncPassengerRequest: {str(e)}")
            return matching_pb2.SyncResponse(success=False)
        finally:
            db.close()

    # --- HÀM 2: ĐỒNG BỘ LỘ TRÌNH TÀI XẾ & KÍCH HOẠT AGENT ---
    def SyncDriverTrip(self, request, context):
        logger.info(f"--- [gRPC SYNC] Driver Trip ID: {request.trip_id} ---")
        db = SessionLocal()
        try:
            # 1. Decode Polyline và lưu DB
            coords = polyline.decode(request.route_polyline)
            wkt_coords = ", ".join([f"{c[1]} {c[0]}" for c in coords])
            line_wkt = f"LINESTRING({wkt_coords})"
            
            dt_str = request.departure_time.replace('Z', '+00:00')
            departure_dt = datetime.datetime.fromisoformat(dt_str)

            new_trip = DriverTripAI(
                trip_id=request.trip_id,
                driver_name=request.driver_name,
                available_seats=request.available_seats,
                departure_time=departure_dt,
                route_geom=line_wkt
            )
            db.merge(new_trip)
            db.commit()
            logger.info(f"✅ Đã lưu lộ trình Trip {request.trip_id}")

            # 2. TRIGGER AGENT (Xử lý bất đồng bộ)
            # Chúng ta đẩy vào event loop để Agent bắt đầu suy luận (Slow Path)
            initial_state = {
                "trip_id": request.trip_id,
                "candidates": [],
                "relevant_policies": "",
                "messages": []
            }
            # Gọi Agent không chặn (Fire and forget)
            asyncio.run_coroutine_threadsafe(
                self._run_agent_matching(initial_state), 
                self.loop
            )

            # Trả về thành công cho Core Service ngay lập tức
            return matching_pb2.SyncResponse(success=True)

        except Exception as e:
            db.rollback()
            logger.error(f"❌ Lỗi SyncDriverTrip: {str(e)}")
            return matching_pb2.SyncResponse(success=False)
        finally:
            db.close()

    async def _run_agent_matching(self, state):
        """Hàm thực thi LangGraph Agent"""
        try:
            logger.info(f"🧠 Agent đang bắt đầu suy luận cho Trip {state['trip_id']}...")
            result = await matching_agent.ainvoke(state)
            logger.info(f"✅ Agent hoàn tất Matching cho Trip {state['trip_id']}")
            # Kết quả này sau đó có thể được push qua Websocket hoặc lưu vào Redis để App hiển thị
            return result
        except Exception as e:
            logger.error(f"❌ Agent Error: {str(e)}")


    def _run_ai_agent_background(self, trip_id, candidates):
            """Hàm chạy ngầm cho AI Agent"""
            try:
                print(f"🧠 [Slow Path] AI Agent bắt đầu suy luận cho Trip {trip_id}...")
                result = matching_agent.invoke({"trip_id": trip_id, "candidates": candidates})
                
                # Ở đây bạn có thể update kết quả vào Redis hoặc DB 
                # để Frontend lấy được reasoning qua Socket/Long-polling
                decision = result.get("final_decision")
                print(f"✅ [Slow Path] AI Hoàn tất: {decision}")
            except Exception as e:
                print(f"❌ [Slow Path] AI Error: {e}")
    # --- HÀM 3: LẤY KẾT QUẢ MATCHING (Fast Path) ---
    def GetPotentialPassengers(self, request, context):
        """
        Fast Path: Trả về danh sách ID khách hàng tiềm năng ngay lập tức (< 500ms)
        """
        trip_id = request.trip_id
        print(f"🚀 [Fast Path] Nhận yêu cầu matching cho Trip: {trip_id}")

        # 1. Lấy nhanh ứng viên từ PostGIS (Rất nhanh)
        candidates = find_nearby_passengers(trip_id)
        candidate_ids = [str(c['id']) for c in candidates]

        # 2. Kích hoạt AI Agent chạy ngầm (Slow Path)
        # Không dùng 'await' hay block luồng chính
        thread = threading.Thread(target=self._run_ai_agent_background, args=(trip_id, candidates))
        thread.start()

        # 3. Trả về ngay danh sách ID
        return matching_pb2.MatchResponse(
            is_match=len(candidate_ids) > 0,
            candidate_ids=candidate_ids,
            reasoning="AI đang phân tích chuyên sâu... Danh sách sơ bộ đã sẵn sàng."
        )

# --- KHỞI CHẠY SERVER ---
def serve():
    init_db()
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    matching_pb2_grpc.add_MatchingServiceServicer_to_server(MatchingService(), server)
    server.add_insecure_port('[::]:50051') 
    logger.info("🚀 AI Matching Service gRPC + Agentic Workflow is Ready!")
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()