from sqlalchemy import text

# Database nằm ngoài app/ → import trực tiếp từ root
from database import SessionLocal   # ← Đây là sửa chính

def find_nearby_passengers(trip_id: int, buffer_meters: int = 1500):
    """
    Tìm khách hàng gần lộ trình của tài xế
    """
    db = SessionLocal()
    try:
        print(f"🔍 Đang query PostGIS cho trip_id={trip_id}, buffer={buffer_meters}m")
        
        query = text("""
            SELECT 
                p.request_id, 
                p.passenger_name, 
                p.seats_requested,
                ST_AsText(p.start_geom) as pickup_point,
                ST_Distance(p.start_geom::geography, d.route_geom::geography) as distance_from_route
            FROM passenger_requests_ai p, driver_trips_ai d
            WHERE d.trip_id = :trip_id
              AND ST_DWithin(p.start_geom::geography, d.route_geom::geography, :buffer)
            ORDER BY distance_from_route ASC
            LIMIT 10;
        """)
        
        results = db.execute(query, {"trip_id": trip_id, "buffer": buffer_meters}).fetchall()
        
        candidates = []
        for r in results:
            candidates.append({
                "id": r.request_id,
                "name": r.passenger_name,
                "seats": r.seats_requested,
                "dist_to_route": f"{round(float(r.distance_from_route or 0), 2)}m",
                "pickup_point": r.pickup_point
            })
        
        print(f"✅ Tìm thấy {len(candidates)} khách hàng tiềm năng gần lộ trình")
        return candidates
        
    except Exception as e:
        print(f"❌ Lỗi khi query database: {str(e)}")
        import traceback
        traceback.print_exc()
        return []
    finally:
        db.close()