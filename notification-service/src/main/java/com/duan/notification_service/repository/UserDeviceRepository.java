package com.duan.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duan.notification_service.entity.UserDevice;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    // Tìm tất cả thiết bị của một người dùng để gửi thông báo đồng loạt
    List<UserDevice> findByUserId(Long userId);

    // Tìm theo token để xóa khi Firebase báo Unregistered (Token hết hạn)
    Optional<UserDevice> findByDeviceToken(String deviceToken);
    
    // Xóa token
    @org.springframework.transaction.annotation.Transactional
    void deleteByDeviceToken(String deviceToken);
}