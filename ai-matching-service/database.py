import os
import time
import logging
from sqlalchemy import create_engine, Column, BigInteger, String, DateTime, Integer, text
from sqlalchemy.orm import declarative_base, sessionmaker
from geoalchemy2 import Geometry
from dotenv import load_dotenv

load_dotenv()

# Ưu tiên lấy từ biến môi trường của Docker Compose (db-ai:5432)
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://ai_user:ai_password@db-ai:5432/dhay_ai_db")

# Tạo engine với cấu hình pool tốt hơn cho service AI
engine = create_engine(DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# --- MODELS ---
class PassengerRequestAI(Base):
    __tablename__ = "passenger_requests_ai"
    request_id = Column(BigInteger, primary_key=True)
    passenger_name = Column(String)
    start_geom = Column(Geometry('POINT', srid=4326))
    end_geom = Column(Geometry('POINT', srid=4326))
    departure_time = Column(DateTime)
    seats_requested = Column(Integer)

class DriverTripAI(Base):
    __tablename__ = "driver_trips_ai"
    trip_id = Column(BigInteger, primary_key=True)
    driver_name = Column(String)
    available_seats = Column(Integer)
    departure_time = Column(DateTime)
    route_geom = Column(Geometry('LINESTRING', srid=4326))

def init_db():
    retries = 10
    while retries > 0:
        try:
            # Bước 1: Tạo Extension (phải commit và đóng kết nối này trước)
            with engine.connect() as conn:
                conn.execute(text("CREATE EXTENSION IF NOT EXISTS postgis;"))
                conn.commit()
            
            # Bước 2: Tạo bảng (Dùng một kết nối mới từ metadata)
            Base.metadata.create_all(bind=engine)
            print(">>> Database AI: Khởi tạo bảng thành công!")
            return 
        except Exception as e:
            print(f">>> Đang chờ DB... Lỗi: {e}")
            retries -= 1
            time.sleep(5)
            
    raise Exception("Không thể kết nối đến Database AI sau nhiều lần thử.")