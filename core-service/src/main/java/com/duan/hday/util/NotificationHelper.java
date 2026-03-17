package com.duan.hday.util;

import com.duan.hday.dto.event.NotificationEvent;
import com.duan.hday.entity.enums.NotificationType;
import java.util.Map;

public class NotificationHelper {

    public static NotificationEvent buildEvent(Long userId, NotificationType type, Map<String, String> data, Object... args) {
        return NotificationEvent.builder()
                .userId(userId)
                .title(type.getTitle())
                .body(type.formatBody(args)) // Tự động điền tên, mã chuyến... vào template
                .data(data)
                .build();
    }
}