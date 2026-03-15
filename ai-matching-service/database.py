import os
import time
from sqlalchemy import create_engine, Column, BigInteger, String, DateTime, Integer, text
from sqlalchemy.orm import declarative_base, sessionmaker # Sửa chỗ này
from geoalchemy2 import Geometry
from dotenv import load_dotenv

load_dotenv()

DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://ai_user:ai_password@localhost:5432/hday_ai_db")

# Tạo engine
engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# --- MODEL GIỮ NGUYÊN ---
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
    # Thử kết nối lại vài lần nếu DB chưa sẵn sàng (chờ Docker DB khởi động)
    retries = 5
    while retries > 0:
        try:
            with engine.connect() as conn:
                # Bắt buộc có dòng này để dùng được POINT và LINESTRING
                conn.execute(text("CREATE EXTENSION IF NOT EXISTS postgis;"))
                conn.commit()
            Base.metadata.create_all(bind=engine)
            print("✅ Database AI đã được khởi tạo thành công!")
            break
        except Exception as e:
            print(f"⚠️ Đang chờ Database... ({retries})")
            retries -= 1
            time.sleep(5)

if __name__ == "__main__":
    init_db()