FROM python:3.11-slim

WORKDIR /app

# Cài đặt thư viện hệ thống để hỗ trợ build psycopg2 và shapely
RUN apt-get update && apt-get install -y \
    libpq-dev \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Cập nhật pip và cài đặt thư viện
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy code
COPY . .

RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. matching.proto

CMD ["python", "main.py"]