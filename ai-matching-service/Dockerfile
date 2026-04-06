# 1. Base Image
FROM python:3.11-slim

# 2. Môi trường Python
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PYTHONPATH=/app

WORKDIR /app

# 3. Dependencies hệ thống (Cần cho PostGIS/Shapely)
RUN apt-get update && apt-get install -y \
    libpq-dev \
    gcc \
    libgeos-dev \
    && rm -rf /var/lib/apt/lists/*

# 4. Thư viện Python (Sử dụng Cache)
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir --use-pep517 -r requirements.txt

# 5. gRPC Proto
COPY matching.proto .
RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. matching.proto

# 6. COPY CODE - ĐẢM BẢO ĐẦY ĐỦ FILE Ở GỐC
# Copy toàn bộ file .py ở thư mục gốc (main, database, matching_service, test_agent...)
COPY *.py ./
# Copy folder app (chứa agents, tools, state...)
COPY app/ ./app/

COPY docs/ ./docs/
# Copy file môi trường

# 7. Port
EXPOSE 8000
EXPOSE 50051

# 8. Khởi chạy
CMD ["python", "main.py"]