from sqlalchemy import Column, BigInteger, String, DateTime, Integer
from geoalchemy2 import Geometry
from database import Base, engine # Import từ file database.py ở trên

class PassengerRequestAI(Base):
    __tablename__ = "passenger_requests_ai"

    request_id = Column(BigInteger, primary_key=True)
    passenger_name = Column(String)
    start_geom = Column(Geometry('POINT', srid=4326))
    end_geom = Column(Geometry('POINT', srid=4326))
    departure_time = Column(DateTime)
    seats_requested = Column(Integer)

# Lệnh này sẽ quét các class kế thừa Base và tạo bảng trong DB
if __name__ == "__main__":
    Base.metadata.create_all(bind=engine)
    print("Đã tạo bảng thành công!")