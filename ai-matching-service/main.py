import grpc
from concurrent import futures
import logging
import threading
import time
import requests
import uvicorn
import os
from fastapi import FastAPI, Header, HTTPException

# Import logic từ các file nội bộ
from database import init_db 
import matching_pb2_grpc
from matching_service import MatchingService 

# --- CẤU HÌNH HỆ THỐNG ---
logging.basicConfig(
    level=logging.INFO, 
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger("AI-Matching-Main")

SERVICE_NAME = os.getenv("SERVICE_NAME", "ai-matching-service")
HTTP_PORT = 8000
GRPC_PORT = 50051
# Sửa lại URL Eureka để linh hoạt hơn
EUREKA_SERVER = os.getenv("EUREKA_SERVER", "http://discovery-server:8761/eureka/apps/")

# --- PHẦN 1: LOGIC ĐĂNG KÝ EUREKA ---
def register_with_eureka():
    instance_id = f"{SERVICE_NAME}:python-ai"
    heartbeat_url = f"{EUREKA_SERVER}{SERVICE_NAME}"
    
    registration_data = {
        "instance": {
            "instanceId": instance_id,
            "hostName": "ai-matching", 
            "app": SERVICE_NAME.upper(),
            "ipAddr": "ai-matching",
            "status": "UP",
            "port": {"$": HTTP_PORT, "@enabled": "true"},
            "dataCenterInfo": {
                "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo", 
                "name": "MyOwn"
            }
        }
    }
    
    while True:
        try:
            # Thử gửi Heartbeat trước (PUT), nếu 404 thì mới POST đăng ký mới
            res = requests.put(f"{heartbeat_url}/{instance_id}", timeout=5)
            if res.status_code == 404:
                requests.post(heartbeat_url, json=registration_data, timeout=5)
                logger.info(f"Eureka: Đã đăng ký mới thành công {instance_id}")
            else:
                logger.debug("Eureka: Heartbeat sent.")
        except Exception as e:
            logger.error(f"Eureka Heartbeat Error: {e}")
        
        time.sleep(30) 

# --- PHẦN 2: KHỞI TẠO FASTAPI ---
app = FastAPI(title="DHAY AI-Matching Service")
# Khởi tạo một instance duy nhất để dùng chung cho cả HTTP và gRPC
matching_logic = MatchingService()

@app.get("/api/v1/matching/health")
def health_check():
    return {"status": "UP", "service": SERVICE_NAME}

# --- PHẦN 3: HÀM CHẠY gRPC SERVER ---
def serve_grpc():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    matching_pb2_grpc.add_MatchingServiceServicer_to_server(matching_logic, server)
    
    # SỬA TẠI ĐÂY: Dùng [::] để gRPC chấp nhận mọi kết nối trong mạng Docker
    server.add_insecure_port(f'[::]:{GRPC_PORT}')
    
    logger.info(f"gRPC Server đang lắng nghe tại port {GRPC_PORT}")
    server.start()
    server.wait_for_termination()

# --- PHẦN 4: ĐIỂM KHỞI CHẠY CHÍNH ---
if __name__ == '__main__':
    # 1. Khởi tạo DB
    try:
        init_db() 
    except Exception as e:
        logger.error(f"Lỗi khởi tạo DB: {e}")

    # 2. Chạy gRPC trong thread riêng
    grpc_thread = threading.Thread(target=serve_grpc)
    grpc_thread.daemon = True
    grpc_thread.start()

    # 3. Chạy Eureka trong thread riêng
    eureka_thread = threading.Thread(target=register_with_eureka)
    eureka_thread.daemon = True
    eureka_thread.start()
    
    # 4. Chạy FastAPI (Tiến trình chính giữ cho container luôn sống)
    logger.info(f"Khởi chạy FastAPI tại port {HTTP_PORT}...")
    uvicorn.run(app, host="0.0.0.0", port=HTTP_PORT)