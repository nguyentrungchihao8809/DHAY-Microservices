package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.duan.hday.entity.enums.TripStatus;

@Entity
@Table(name = "trips")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Trip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    /**
     * Mỗi chuyến đi phải gắn liền với 1 xe cụ thể của tài xế đó.
     * Điều này giải quyết vấn đề Driver có nhiều xe.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /**
     * Nhận dữ liệu tọa độ từ Mapbox qua DTO và lưu vào đây.
     * CascadeType.ALL giúp lưu Location cùng lúc với Trip.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "start_location_id", nullable = false)
    private Location startLocation;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "end_location_id", nullable = false)
    private Location endLocation;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    private LocalDateTime estimatedArrivalTime;

    /**
     * totalSeats: Số ghế driver đăng ký cho chuyến này.
     * availableSeats: Số ghế còn lại sau khi có người đặt thành công.
     */
    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    /**
     * Giá cho mỗi ghế để tối ưu chi phí di chuyển.
     */
    // @Column(nullable = false, precision = 12, scale = 2)
    // private BigDecimal pricePerSeat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(columnDefinition = "TEXT")
    private String routePolyline;

    @Column(name = "distance_km")
    private Double distanceKm; // Khoảng cách thực tế từ OSRM

    @Column(name = "duration_minutes")
    private Integer durationMinutes; // Thời gian di chuyển thực tế từ OSRM

    @Column(name = "route_name", length = 500)
    private String routeName; // Tên tuyến đường đã chọn
    /* ================= RELATIONS ================= */

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    private List<Booking> bookings;

}