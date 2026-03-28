import grpc
from concurrent import futures
import logging
import threading
import time
import requests
import uvicorn
from fastapi import FastAPI, Header, HTTPException, Request

# Thư viện gRPC của bạn
import matching_pb2_grpc
from matching_service import MatchingService 

# --- CẤU HÌNH ---
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
SERVICE_NAME = "ai-matching-service"
HTTP_PORT = 8000
GRPC_PORT = 50051
EUREKA_SERVER = "http://discovery-server:8761/eureka/apps/"

# --- PHẦN 1: LOGIC ĐĂNG KÝ EUREKA (Dành cho Gateway) ---
def register_with_eureka():
    """Hàm này giúp Gateway 'nhìn thấy' Python qua tên service"""
    heartbeat_url = f"{EUREKA_SERVER}{SERVICE_NAME}"
    registration_data = {
        "instance": {
            "instanceId": f"{SERVICE_NAME}:python-ai",
            "hostName": "ai-matching", # Tên container trong docker-compose
            "app": SERVICE_NAME.upper(),
            "ipAddr": "ai-matching",
            "vipAddress": SERVICE_NAME.upper(),
            "secureVipAddress": SERVICE_NAME.upper(),
            "status": "UP",
            "overriddenstatus": "UNKNOWN",
            "port": {"$": HTTP_PORT, "@enabled": "true"},
            "dataCenterInfo": {"@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo", "name": "MyOwn"}
        }
    }
    
    while True:
        try:
            # Gửi yêu cầu đăng ký
            response = requests.post(heartbeat_url, json=registration_data, timeout=5)
            if response.status_code in [200, 204]:
                logging.info(f"Đã đăng ký thành công với Eureka: {SERVICE_NAME}")
            else:
                # Nếu đã đăng ký rồi, gửi Heartbeat (PUT)
                requests.put(f"{heartbeat_url}/{SERVICE_NAME}:python-ai", timeout=5)
        except Exception as e:
            logging.error(f"Lỗi kết nối Eureka: {e}")
        
        time.sleep(30) # Gửi định kỳ mỗi 30s để duy trì trạng thái 'UP'

# --- PHẦN 2: KHỞI TẠO FASTAPI ---
app = FastAPI(title="Dhay AI Matching API")
matching_logic = MatchingService()

@app.get("/api/v1/matching/health") # Thêm prefix cho đúng với Gateway
def health_check():
    return {"status": "UP"}

@app.post("/api/v1/matching/find")
async def find_match_http(
    x_user_id: str = Header(None, alias="X-User-Id"), # Sử dụng alias để khớp chính xác Header từ Gateway
    request_data: dict = None
):
    # Log để kiểm tra xem Gateway có gửi đúng UserID xuống không
    logging.info(f">>> AI Service nhan duoc UserID tu Gateway: {x_user_id}")
    
    if not x_user_id:
        raise HTTPException(status_code=401, detail="Unauthorized: No User ID from Gateway")
    
    if x_user_id == "GUEST":
        return {"status": "denied", "message": "Vui lòng đăng nhập để sử dụng tính năng này"}
    
    return {"status": "processing", "user_id": x_user_id}

# --- PHẦN 3: HÀM CHẠY CÁC SERVER ---
def serve_grpc():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    matching_pb2_grpc.add_MatchingServiceServicer_to_server(matching_logic, server)
    server.add_insecure_port(f'0.0.0.0:{GRPC_PORT}')
    logging.info(f"gRPC AI Matching đang lắng nghe tại {GRPC_PORT}...")
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    # 1. Chạy gRPC trong Thread riêng
    threading.Thread(target=serve_grpc, daemon=True).start()
    
    # 2. Chạy Eureka Registration trong Thread riêng
    threading.Thread(target=register_with_eureka, daemon=True).start()
    
    # 3. Chạy FastAPI ở Thread chính
    logging.info(f"FastAPI đang khởi chạy tại {HTTP_PORT}...")
    uvicorn.run(app, host="0.0.0.0", port=HTTP_PORT)