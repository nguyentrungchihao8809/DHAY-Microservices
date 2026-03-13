import logging
import datetime
import matching_pb2
import matching_pb2_grpc
from database import SessionLocal, PassengerRequestAI

# Cấu hình logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

class MatchingService(matching_pb2_grpc.MatchingServiceServicer):

    # --- HÀM 1: ĐỒNG BỘ TỪ JAVA SANG AI DATABASE ---
    def SyncPassengerRequest(self, request, context):
        logging.info(f"--- [📥 SYNC] Nhận dữ liệu khách hàng ID: {request.request_id} ---")
        db = SessionLocal()
        try:
            # Xử lý format thời gian
            dt_str = request.departure_time.replace('Z', '') 
            
            new_req = PassengerRequestAI(
                request_id=request.request_id,
                passenger_name=request.passenger_name,
                departure_time=datetime.datetime.fromisoformat(dt_str),
                seats_requested=request.seats_requested,
                # Định dạng PostGIS: POINT(kinh_độ vĩ_độ)
                start_geom=f'POINT({request.start_location.lng} {request.start_location.lat})',
                end_geom=f'POINT({request.end_location.lng} {request.end_location.lat})'
            )
            
            db.merge(new_req) # Thêm mới hoặc cập nhật nếu trùng ID
            db.commit()
            
            logging.info(f"✅ Đã lưu vào AI Database (hday_ai_db) thành công.")
            return matching_pb2.SyncResponse(success=True)
            
        except Exception as e:
            db.rollback()
            logging.error(f"❌ Lỗi khi lưu vào AI Database: {str(e)}")
            return matching_pb2.SyncResponse(success=False)
        finally:
            db.close()

    # --- HÀM 2: TÌM KIẾM KHÁCH HÀNG PHÙ HỢP (MATCHING) ---
    def GetPotentialPassengers(self, request, context):
        logging.info(f"--- [🔔 RECEIVED] Java tìm khách cho Trip ID: {request.trip_id} ---")
        
        try:
            response = matching_pb2.MatchResponse()
            
            # Logic Mockup trả về 1 kết quả để test kết nối
            match1 = response.matches.add()
            match1.request_id = 101
            match1.passenger_name = "Khách hàng Demo (AI Service)"
            match1.match_score = 0.98
            match1.reasoning = "Nằm trên lộ trình di chuyển của bạn"

            logging.info(f"✅ Đã gửi {len(response.matches)} kết quả mockup về cho Java.")
            return response

        except Exception as e:
            logging.error(f"❌ Lỗi xử lý Matching: {str(e)}")
            return matching_pb2.MatchResponse()