package com.duan.hday.repository.driver;


import com.duan.hday.entity.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {
    // existsById đã có sẵn trong JpaRepository
}
