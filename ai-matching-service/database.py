import os
from sqlalchemy import create_engine, Column, BigInteger, String, DateTime, Integer
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from geoalchemy2 import Geometry
from dotenv import load_dotenv

load_dotenv()

# 1. Cấu hình kết nối
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://ai_user:ai_password@localhost:5432/hday_ai_db")

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# 2. Định nghĩa các Model
class PassengerRequestAI(Base):
    __tablename__ = "passenger_requests_ai"

    request_id = Column(BigInteger, primary_key=True)
    passenger_name = Column(String)
    # PostGIS: POINT(lng lat)
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
    # PostGIS: LINESTRING(lng lat, lng lat, ...)
    route_geom = Column(Geometry('LINESTRING', srid=4326))

# 3. Tạo bảng (Phải đặt SAU khi đã định nghĩa tất cả các Class Model)
def init_db():
    Base.metadata.create_all(bind=engine)

if __name__ == "__main__":
    init_db()
    print("✅ Database AI đã được khởi tạo thành công!")