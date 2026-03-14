import logging
import datetime
import matching_pb2
import matching_pb2_grpc
from database import SessionLocal, PassengerRequestAI, DriverTripAI  # Nhớ import DriverTripAI
import polyline

# Cấu hình logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

class MatchingService(matching_pb2_grpc.MatchingServiceServicer):

    # --- HÀM 1: ĐỒNG BỘ YÊU CẦU KHÁCH HÀNG ---
    def SyncPassengerRequest(self, request, context):
        logging.info(f"--- [📥 SYNC PASSENGER] ID: {request.request_id} ---")
        db = SessionLocal()
        try:
            # Xử lý format thời gian từ Java (ISO_LOCAL_DATE_TIME)
            dt_str = request.departure_time.replace('Z', '') 
            
            new_req = PassengerRequestAI(
                request_id=request.request_id,
                passenger_name=request.passenger_name,
                departure_time=datetime.datetime.fromisoformat(dt_str),
                seats_requested=request.seats_requested,
                # PostGIS dùng (Kinh độ - Vĩ độ)
                start_geom=f'POINT({request.start_location.lng} {request.start_location.lat})',
                end_geom=f'POINT({request.end_location.lng} {request.end_location.lat})'
            )
            
            db.merge(new_req)
            db.commit()
            logging.info(f"✅ Đã lưu Passenger {request.request_id} vào AI DB.")
            return matching_pb2.SyncResponse(success=True)
            
        except Exception as e:
            db.rollback()
            logging.error(f"❌ Lỗi Sync Passenger: {str(e)}")
            return matching_pb2.SyncResponse(success=False)
        finally:
            db.close()

    # --- HÀM 2: ĐỒNG BỘ LỘ TRÌNH TÀI XẾ (MỚI) ---
    def SyncDriverTrip(self, request, context):
        logging.info(f"--- [📥 SYNC DRIVER TRIP] ID: {request.trip_id} ---")
        db = SessionLocal()
        try:
            # 1. Giải mã Polyline sang LINESTRING
            coords = polyline.decode(request.route_polyline)
            # Chuyển (lat, lng) của polyline -> (lng lat) cho PostGIS
            wkt_coords = ", ".join([f"{c[1]} {c[0]}" for c in coords])
            line_wkt = f"LINESTRING({wkt_coords})"
            
            # 2. Xử lý thời gian
            dt_str = request.departure_time.replace('Z', '')

            new_trip = DriverTripAI(
                trip_id=request.trip_id,
                driver_name=request.driver_name,
                available_seats=request.available_seats,
                departure_time=datetime.datetime.fromisoformat(dt_str),
                route_geom=line_wkt
            )
            
            db.merge(new_trip)
            db.commit()
            logging.info(f"✅ Đã lưu lộ trình Trip {request.trip_id} (Length: {len(coords)} points).")
            return matching_pb2.SyncResponse(success=True)
        except Exception as e:
            db.rollback()
            logging.error(f"❌ Lỗi Sync Driver Trip: {str(e)}")
            return matching_pb2.SyncResponse(success=False)
        finally:
            db.close()

    # --- HÀM 3: TÌM KIẾM KHÁCH HÀNG (MATCHING LOGIC) ---
    def GetPotentialPassengers(self, request, context):
        logging.info(f"--- [🔍 MATCHING REQUEST] Trip ID: {request.trip_id} ---")
        # Tạm thời để Mockup, chúng ta sẽ viết câu Query PostGIS ở bước kế tiếp
        response = matching_pb2.MatchResponse()
        match1 = response.matches.add()
        match1.request_id = 999
        match1.passenger_name = "AI Tester"
        match1.match_score = 1.0
        match1.reasoning = "Hệ thống đang sẵn sàng tính toán lộ trình."
        return response