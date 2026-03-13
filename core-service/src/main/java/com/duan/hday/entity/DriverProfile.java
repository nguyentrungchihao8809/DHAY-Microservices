package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "driver_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DriverProfile extends BaseEntity {
    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String licenseNumber; // Bằng lái xe

    @Builder.Default
    private Double ratingAvg = 5.0;

    @Builder.Default
    private Integer totalTrips = 0;

    @Builder.Default
    private Boolean isActive = true;
}
