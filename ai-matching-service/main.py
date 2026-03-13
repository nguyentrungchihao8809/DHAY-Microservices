import grpc
from concurrent import futures
import logging
import matching_pb2
import matching_pb2_grpc

# Cấu hình log để theo dõi trong Docker
logging.basicConfig(
    level=logging.INFO, 
    format='%(asctime)s - %(levelname)s - %(message)s'
)

class MatchingService(matching_pb2_grpc.MatchingServiceServicer):
    
    def GetPotentialPassengers(self, request, context):
        # Log rõ ràng để biết Java đã gọi tới
        logging.info(f"--- [🔔 RECEIVED] Nhận yêu cầu từ Java ---")
        logging.info(f" - Trip ID: {request.trip_id}")
        logging.info(f" - Available Seats: {request.available_seats}")
        
        try:
            # 1. Khởi tạo đối tượng Response
            response = matching_pb2.MatchResponse()
            
            # 2. Thêm dữ liệu Mockup vào danh sách 'matches' (repeated)
            # Lưu ý: Với trường repeated, ta dùng method .add() thay vì gán trực tiếp
            match1 = response.matches.add()
            match1.request_id = 101
            match1.passenger_name = "Khách hàng Demo (AI)"
            match1.match_score = 0.95
            match1.reasoning = "Nằm trên lộ trình của bạn (Phạm vi 2.5km)"

            logging.info(f"✅ Đã chuẩn bị xong {len(response.matches)} kết quả trả về.")
            
            return response

        except Exception as e:
            logging.error(f"❌ Lỗi xử lý Logic: {str(e)}")
            # Trả về response trống nếu có lỗi để Java không bị crash kết nối
            return matching_pb2.MatchResponse()

def serve():
    # 10 workers để xử lý nhiều yêu cầu cùng lúc
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    
    # Đăng ký Service vào Server
    matching_pb2_grpc.add_MatchingServiceServicer_to_server(MatchingService(), server)
    
    # Lắng nghe tại port 50051 (Phải trùng với file .properties bên Java)
    server.add_insecure_port('0.0.0.0:50051')
    
    logging.info("🚀 gRPC AI Matching Service đã chạy và sẵn sàng tại cổng 50051")
    
    server.start()
    # Giữ server chạy liên tục
    server.wait_for_termination()

if __name__ == '__main__':
    serve()