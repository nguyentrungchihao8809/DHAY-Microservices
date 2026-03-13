import os
from sqlalchemy import create_engine, Column, BigInteger, String, DateTime, Integer
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from geoalchemy2 import Geometry
from dotenv import load_dotenv

load_dotenv()

# Cấu hình kết nối
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://ai_user:ai_password@db-ai:5432/hday_ai_db")

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Định nghĩa Model ngay tại đây để file matching_service.py import được luôn
class PassengerRequestAI(Base):
    __tablename__ = "passenger_requests_ai"

    request_id = Column(BigInteger, primary_key=True)
    passenger_name = Column(String)
    # srid=4326 là hệ tọa độ chuẩn GPS toàn cầu (WGS84)
    start_geom = Column(Geometry('POINT', srid=4326))
    end_geom = Column(Geometry('POINT', srid=4326))
    departure_time = Column(DateTime)
    seats_requested = Column(Integer)

# Tự động tạo bảng khi service khởi chạy
Base.metadata.create_all(bind=engine)