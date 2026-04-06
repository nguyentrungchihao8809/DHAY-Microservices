from sqlalchemy import text
from app.core.database import SessionLocal

def find_nearby_passengers(trip_id: int, buffer_meters: int = 1000):
    """
    Tìm khách hàng có điểm đón nằm trong phạm vi 'buffer_meters' 
    xung quanh lộ trình của tài xế.
    """
    db = SessionLocal()
    try:
        query = text("""
            SELECT 
                p.request_id, 
                p.passenger_name, 
                p.seats_requested,
                ST_AsText(p.start_geom) as pickup_point,
                ST_Distance(p.start_geom, d.route_geom) as distance_from_route
            FROM passenger_requests_ai p, driver_trips_ai d
            WHERE d.trip_id = :trip_id
            AND ST_DWithin(p.start_geom::geography, d.route_geom::geography, :buffer)
            ORDER BY distance_from_route ASC;
        """)
        
        results = db.execute(query, {"trip_id": trip_id, "buffer": buffer_meters}).fetchall()
        
        # Format lại để Agent dễ đọc
        candidates = []
        for r in results:
            candidates.append({
                "id": r.request_id,
                "name": r.passenger_name,
                "seats": r.seats_requested,
                "dist_to_route": f"{round(r.distance_from_route, 2)}m"
            })
        return candidates
    finally:
        db.close()