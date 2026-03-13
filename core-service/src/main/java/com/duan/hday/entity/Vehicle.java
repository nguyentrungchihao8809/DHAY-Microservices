package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;
import com.duan.hday.entity.enums.VehicleType;

@Entity
@Table(name = "vehicles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver; // Chủ sở hữu xe

   @Column(nullable = false, unique = true)
    private String vehiclePlate;
    private String vehicleBrand; // Hãng xe (Honda, Toyota...)
    private String vehicleModel; // Dòng xe (Civic, SH...)
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;  // Loại xe (Xe máy, Ô tô 4 chỗ...)

    private Integer capacity;    // Số ghế tối đa của xe này

    @Builder.Default
    private Boolean isVerified = false;
}
