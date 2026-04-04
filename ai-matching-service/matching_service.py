import logging
import datetime
import grpc
from concurrent import futures
import matching_pb2
import matching_pb2_grpc
from database import SessionLocal, PassengerRequestAI, DriverTripAI, init_db
import polyline

# --- Cấu hình Logging ---
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger("AI-Matching")

class MatchingService(matching_pb2_grpc.MatchingServiceServicer):

    # --- HÀM 1: ĐỒNG BỘ YÊU CẦU KHÁCH HÀNG ---
    def SyncPassengerRequest(self, request, context):
        logger.info(f"--- [gRPC SYNC] Passenger ID: {request.request_id} ---")
        db = SessionLocal()
        try:
            # Xử lý format thời gian từ Java (ISO 8601)
            dt_str = request.departure_time.replace('Z', '+00:00')
            departure_dt = datetime.datetime.fromisoformat(dt_str)

            new_req = PassengerRequestAI(
                request_id=request.request_id,
                passenger_name=request.passenger_name,
                departure_time=departure_dt,
                seats_requested=request.seats_requested,
                # Sử dụng f-string chuẩn cho PostGIS WKT
                start_geom=f'POINT({request.start_location.lng} {request.start_location.lat})',
                end_geom=f'POINT({request.end_location.lng} {request.end_location.lat})'
            )
            
            # Sử dụng merge để insert hoặc update nếu đã tồn tại
            db.merge(new_req)
            db.commit()
            logger.info(f"✅ Đã lưu/cập nhật Passenger {request.request_id}")
            return matching_pb2.SyncResponse(success=True)

        except Exception as e:
            db.rollback()
            logger.error(f"❌ Lỗi SyncPassengerRequest: {str(e)}")
            # Trả về False thay vì để gRPC Panic
            return matching_pb2.SyncResponse(success=False)
        finally:
            db.close()

    # --- HÀM 2: ĐỒNG BỘ LỘ TRÌNH TÀI XẾ ---
    def SyncDriverTrip(self, request, context):
        logger.info(f"--- [gRPC SYNC] Driver Trip ID: {request.trip_id} ---")
        db = SessionLocal()
        try:
            # Giải mã Polyline sang định dạng WKT LineString cho PostGIS
            coords = polyline.decode(request.route_polyline)
            # Polyline.decode trả về (lat, lng), PostGIS cần (lng lat)
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
            logger.info(f"✅ Đã đồng bộ lộ trình Trip {request.trip_id}")
            return matching_pb2.SyncResponse(success=True)

        except Exception as e:
            db.rollback()
            logger.error(f"❌ Lỗi SyncDriverTrip: {str(e)}")
            return matching_pb2.SyncResponse(success=False)
        finally:
            db.close()

    # --- HÀM 3: TÌM KIẾM KHÁCH HÀNG (TRẢ VỀ LIST REPEATED) ---
    def GetPotentialPassengers(self, request, context):
        logger.info(f"--- [gRPC MATCHING] Yêu cầu cho Trip: {request.trip_id} ---")
        try:
            response = matching_pb2.MatchResponse()
            
            # MÔ PHỎNG LOGIC MATCHING (Sau này bạn viết code SQL PostGIS ở đây)
            # Ví dụ: Thêm một khách hàng giả định vào danh sách repeated
            match_item = response.matches.add() # ĐÂY LÀ CÁCH ADD ĐÚNG CHO REPEATED FIELD
            match_item.request_id = 101
            match_item.passenger_name = "Khách hàng demo"
            match_item.match_score = 0.95
            match_item.reasoning = "Nằm trên lộ trình của tài xế (Mô phỏng)"

            logger.info(f"Tìm thấy {len(response.matches)} khách hàng phù hợp.")
            return response

        except Exception as e:
            logger.error(f"❌ Lỗi GetPotentialPassengers: {str(e)}")
            # Trả về response rỗng thay vì lỗi gRPC INTERNAL
            return matching_pb2.MatchResponse()

# --- KHỞI CHẠY SERVER ---
def serve():
    # 1. Đảm bảo Database đã sẵn sàng
    try:
        init_db()
        logger.info("🗄️ Database AI đã được khởi tạo.")
    except Exception as e:
        logger.error(f"Không thể kết nối Database: {e}")

    # 2. Cấu hình gRPC Server
    # Sử dụng ThreadPoolExecutor để xử lý nhiều request cùng lúc
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    matching_pb2_grpc.add_MatchingServiceServicer_to_server(MatchingService(), server)
    
    # Listen trên port 50051 (Port nội bộ Docker)
    server.add_insecure_port('[::]:50051') 
    logger.info("🚀 AI Matching Service gRPC đang chạy tại cổng 50051...")
    
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()