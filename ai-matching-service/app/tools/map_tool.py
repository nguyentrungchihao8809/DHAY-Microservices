# app/tools/maps_tool.py
import requests
import polyline

def get_osrm_metrics(driver_polyline: str, passenger_pickup: list, passenger_dropoff: list):
    """
    Sử dụng OSRM để tính toán độ lệch lộ trình.
    driver_polyline: Tuyến đường gốc của tài xế (Encoded string)
    passenger_pickup: [lng, lat]
    passenger_dropoff: [lng, lat]
    """
    # 1. Giải mã polyline để lấy điểm đầu và cuối của tài xế
    coords = polyline.decode(driver_polyline)
    start_node = f"{coords[0][1]},{coords[0][0]}"
    end_node = f"{coords[-1][1]},{coords[-1][0]}"
    
    p_pickup = f"{passenger_pickup[0]},{passenger_pickup[1]}"
    p_dropoff = f"{passenger_dropoff[0]},{passenger_dropoff[1]}"

    # 2. Gọi OSRM Route API cho lộ trình mới: Start -> Pickup -> Dropoff -> End
    osrm_url = f"http://router.project-osrm.org/route/v1/driving/{start_node};{p_pickup};{p_dropoff};{end_node}?overview=false"
    
    try:
        response = requests.get(osrm_url).json()
        if response.get("code") == "Ok":
            route = response["routes"][0]
            # Khoảng cách tính bằng mét -> km, thời gian giây -> phút
            return {
                "new_distance_km": round(route["distance"] / 1000, 2),
                "new_duration_mins": round(route["duration"] / 60, 2),
                "status": "success"
            }
    except Exception as e:
        return {"status": "error", "message": str(e)}

    return {"status": "failed"}