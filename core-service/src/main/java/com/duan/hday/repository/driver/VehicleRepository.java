package com.duan.hday.repository.driver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.duan.hday.entity.Vehicle;

// Tương tự cho Vehicle
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByVehiclePlate(String vehiclePlate);
}
