import logging
import datetime
import grpc  # THÊM DÒNG NÀY
from concurrent import futures # THÊM DÒNG NÀY
import matching_pb2
import matching_pb2_grpc
from database import SessionLocal, PassengerRequestAI, DriverTripAI, init_db
import polyline

# Cấu hình logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

class MatchingService(matching_pb2_grpc.MatchingServiceServicer):

    # --- HÀM 1: ĐỒNG BỘ YÊU CẦU KHÁCH HÀNG ---
    def SyncPassengerRequest(self, request, context):
        logging.info(f"--- [SYNC PASSENGER] ID: {request.request_id} ---")
        db = SessionLocal()
        try:
            dt_str = request.departure_time.replace('Z', '') 
            new_req = PassengerRequestAI(
                request_id=request.request_id,
                passenger_name=request.passenger_name,
                departure_time=datetime.datetime.fromisoformat(dt_str),
                seats_requested=request.seats_requested,
                start_geom=f'POINT({request.start_location.lng} {request.start_location.lat})',
                end_geom=f'POINT({request.end_location.lng} {request.end_location.lat})'
            )
            db.merge(new_req)
            db.commit()
            logging.info(f"Đã lưu Passenger {request.request_id} vào AI DB.")
            return matching_pb2.SyncResponse(success=True)
        except Exception as e:
            db.rollback()
            logging.error(f"Lỗi Sync Passenger: {str(e)}")
            return matching_pb2.SyncResponse(success=False)
        finally:
            db.close()

    # --- HÀM 2: ĐỒNG BỘ LỘ TRÌNH TÀI XẾ ---
    def SyncDriverTrip(self, request, context):
        logging.info(f"--- [SYNC DRIVER TRIP] ID: {request.trip_id} ---")
        db = SessionLocal()
        try:
            coords = polyline.decode(request.route_polyline)
            wkt_coords = ", ".join([f"{c[1]} {c[0]}" for c in coords])
            line_wkt = f"LINESTRING({wkt_coords})"
            dt = datetime.datetime.fromisoformat(request.departure_time.replace('Z', ''))

            existing = db.query(DriverTripAI).filter(DriverTripAI.trip_id == request.trip_id).first()
            if existing:
                existing.driver_name = request.driver_name
                existing.available_seats = request.available_seats
                existing.departure_time = dt
                existing.route_geom = line_wkt
            else:
                new_trip = DriverTripAI(
                    trip_id=request.trip_id,
                    driver_name=request.driver_name,
                    available_seats=request.available_seats,
                    departure_time=dt,
                    route_geom=line_wkt
                )
                db.add(new_trip)
            db.commit()
            return matching_pb2.SyncResponse(success=True)
        except Exception as e:
            db.rollback()
            logging.error(f"Lỗi Sync Driver Trip: {str(e)}")
            return matching_pb2.SyncResponse(success=False)
        finally:
            db.close()

    # --- HÀM 3: TÌM KIẾM KHÁCH HÀNG ---
    def GetPotentialPassengers(self, request, context):
        logging.info(f"--- [MATCHING REQUEST] Trip ID: {request.trip_id} ---")
        response = matching_pb2.MatchResponse()
        match1 = response.matches.add()
        match1.request_id = 999
        match1.passenger_name = "AI Tester"
        match1.match_score = 1.0
        match1.reasoning = "Hệ thống đang sẵn sàng tính toán lộ trình."
        return response

# --- HÀM KHỞI CHẠY SERVER (NẰM NGOÀI CLASS) ---
def serve():
    # 1. Khởi tạo DB (Tạo bảng nếu chưa có)
    init_db() 
    
    # 2. Khởi tạo gRPC Server
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    matching_pb2_grpc.add_MatchingServiceServicer_to_server(MatchingService(), server)
    
    # Listen trên cổng 50051
    server.add_insecure_port('[::]:50051') 
    logging.info("🚀 AI Matching Service đã sẵn sàng tại cổng 50051...")
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()