package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;

@Entity
@Table(
    name = "locations",
    indexes = {
        @Index(name = "idx_locations_geom", columnList = "geom")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double lat;

    private Double lng;

    @Column(length = 255)
    private String address;

    /**
     * Geometry Point dùng cho PostGIS
     * SRID = 4326 (WGS84 - GPS)
     */
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    /**
     * Tự động đồng bộ geom từ lat/lng
     * Lưu ý: Point(x, y) = (lng, lat)
     */
    @PrePersist
    @PreUpdate
    public void updateGeom() {
        if (lat != null && lng != null) {
            GeometryFactory factory =
                    new GeometryFactory(new PrecisionModel(), 4326);

            this.geom = factory.createPoint(
                    new Coordinate(lng, lat)
            );
        }
    }
}
