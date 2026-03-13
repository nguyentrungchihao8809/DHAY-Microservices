package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "driver_locations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DriverLocation extends BaseEntity {

    @Id
    private Long driverId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "driver_id")
    private User driver;

    private Double lat;
    private Double lng;
    private Double bearing;
}
