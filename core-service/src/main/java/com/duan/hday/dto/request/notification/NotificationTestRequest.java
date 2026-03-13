package com.duan.hday.dto.request.notification;

import lombok.Data;
import java.util.Map;

@Data
public class NotificationTestRequest {
    private Long targetUserId;
    private String title;
    private String body;
    private Map<String, String> data; // Cho phép truyền bất kỳ key-value nào vào đây
}
