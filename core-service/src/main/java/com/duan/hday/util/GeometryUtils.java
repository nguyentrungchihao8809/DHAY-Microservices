package com.duan.hday.util;

import com.google.maps.model.LatLng;
import com.google.maps.internal.PolylineEncoding;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.distance.DistanceOp;

public class GeometryUtils {

    // 1. Cấu hình Geometry Factory
    private static final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
    private static final WKTReader wktReader = new WKTReader(factory);
    private static final double EARTH_RADIUS = 6371000; // Bán kính Trái Đất (mét)

    // 2. Chuyển đổi Polyline từ Google/OSRM sang WKT
    public static String castPolylineToWkt(String encodedPolyline) {
        if (encodedPolyline == null || encodedPolyline.isEmpty()) return null;
        
        List<LatLng> coords = PolylineEncoding.decode(encodedPolyline);
        String points = coords.stream()
                .map(latLng -> latLng.lng + " " + latLng.lat)
                .collect(Collectors.joining(", "));

        return "LINESTRING(" + points + ")";
    }

    public static Geometry wktToGeometry(String wkt) {
        try {
            if (wkt == null || wkt.isEmpty()) return null;
            return wktReader.read(wkt);
        } catch (ParseException e) {
            throw new RuntimeException("Lỗi định dạng WKT: " + e.getMessage());
        }
    }

    // 3. Công thức Haversine tính khoảng cách chuẩn mét (Fix lỗi 111.0)
    public static double calculateDistanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    public static double calculateDistanceMeters(Point p1, Point p2) {
        return calculateDistanceMeters(p1.getY(), p1.getX(), p2.getY(), p2.getX());
    }

    // 4. Tính khoảng cách từ 1 điểm đến đường (đơn vị Mét)
    // 4. Tính khoảng cách từ 1 điểm đến đường (đơn vị Mét)
    public static double distanceFromPointToLineMeters(Point p, LineString line) {
        // Sử dụng DistanceOp theo cách mới để tránh warning deprecated
        DistanceOp distOp = new DistanceOp(line, p);
        Coordinate[] closestCoords = distOp.nearestPoints(); 
        
        // closestCoords[0] là điểm trên line, closestCoords[1] là chính điểm p (hoặc điểm gần nhất thuộc p)
        Coordinate closestOnLine = closestCoords[0];
        
        return calculateDistanceMeters(p.getY(), p.getX(), closestOnLine.y, closestOnLine.x);
    }

    // 5. Tìm Index điểm gần nhất (Dùng cho logic Sequence/Segment)
    public static int findNearestPointIndex(LineString route, Point location) {
        double minDistance = Double.MAX_VALUE;
        int nearestIndex = -1;
        Coordinate[] coords = route.getCoordinates();

        for (int i = 0; i < coords.length; i++) {
            double dist = coords[i].distance(location.getCoordinate());
            if (dist < minDistance) {
                minDistance = dist;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }
}