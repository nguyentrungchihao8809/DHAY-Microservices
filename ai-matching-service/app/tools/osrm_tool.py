import requests
import polyline
import logging
from app.core.config import settings

logger = logging.getLogger(__name__)

class OSRMTool:
    def __init__(self):
        self.base_url = settings.OSRM_SERVER_URL

    def get_route(self, coordinates: list):
        """
        Tính toán lộ trình qua danh sách các tọa độ.
        coordinates: [[lng1, lat1], [lng2, lat2], ...]
        """
        # Format: lng,lat;lng,lat
        coord_str = ";".join([f"{c[0]},{c[1]}" for c in coordinates])
        url = f"{self.base_url}/route/v1/driving/{coord_str}?overview=full&geometries=polyline"
        
        try:
            response = requests.get(url, timeout=5)
            data = response.json()
            
            if data.get("code") != "Ok":
                return None
            
            route = data["routes"][0]
            return {
                "distance_meters": route["distance"],
                "duration_seconds": route["duration"],
                "geometry": route["geometry"] # Chuỗi polyline để vẽ bản đồ
            }
        except Exception as e:
            logger.error(f"OSRM Route Error: {e}")
            return None

    def get_match_metrics(self, driver_loc, pickup_loc, dropoff_loc):
        """
        Hàm đặc biệt dành cho Agent:
        Tính toán xem nếu ghé qua đón khách này thì tổng quãng đường là bao nhiêu.
        """
        res = self.get_route([driver_loc, pickup_loc, dropoff_loc])
        if res:
            return {
                "total_dist_km": round(res["distance_meters"] / 1000, 2),
                "total_time_mins": round(res["duration_seconds"] / 60, 1),
                "polyline": res["geometry"]
            }
        return None

# Khởi tạo instance dùng chung
osrm_tool = OSRMTool()