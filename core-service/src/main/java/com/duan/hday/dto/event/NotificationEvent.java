package com.duan.hday.dto.event;

import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private Long userId;       // ID người nhận
    private String title;      // Tiêu đề thông báo
    private String body;   
    private String type;    // Nội dung hiển thị
    private Map<String, String> data; // Payload đi kèm (tripId, bookingId, type...)
}