FROM python:3.11-slim

# Thiết lập môi trường chạy Python ổn định
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PYTHONPATH=/app

WORKDIR /app

# 1. Cài đặt dependencies hệ thống (Thêm libgeos cho các phép toán hình học)
RUN apt-get update && apt-get install -y \
    libpq-dev \
    gcc \
    libgeos-dev \
    && rm -rf /var/lib/apt/lists/*

# 2. Cài đặt thư viện Python (Sử dụng requirements.txt đã sửa)
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# 3. Copy file proto và biên dịch TRƯỚC khi copy code 
# Việc này đảm bảo version grpc_tools khớp tuyệt đối với code sinh ra
COPY matching.proto .
RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. matching.proto

# 4. Copy toàn bộ mã nguồn còn lại
COPY . .

# Port cho FastAPI và gRPC
EXPOSE 8000
EXPOSE 50051

CMD ["python", "main.py"]