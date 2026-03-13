import grpc
from concurrent import futures
import logging
import matching_pb2_grpc
# Quan trọng: Import MatchingService từ file matching_service.py
from matching_service import MatchingService 

logging.basicConfig(
    level=logging.INFO, 
    format='%(asctime)s - %(levelname)s - %(message)s'
)

def serve():
    # Khởi tạo Server với ThreadPool
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    
    # Đăng ký Service đã import ở trên vào Server
    matching_pb2_grpc.add_MatchingServiceServicer_to_server(MatchingService(), server)
    
    # Lắng nghe tại port 50051 (Phải khớp với cấu hình trong Docker/Java)
    server.add_insecure_port('0.0.0.0:50051')
    
    logging.info("🚀 gRPC AI Matching Service đang lắng nghe tại cổng 50051...")
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    serve()