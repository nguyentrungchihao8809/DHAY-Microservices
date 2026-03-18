package com.duan.hday.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.text.MessageFormat;

@Getter
@AllArgsConstructor
public enum NotificationType {
    // Thay %s, %d bằng {0}, {1}, {2}...
    NEW_BOOKING_REQUEST("Yêu cầu đặt chỗ mới", "Khách {0} muốn đặt {1} ghế cho chuyến đi của bạn."),
    BOOKING_CONFIRMED("Đặt chỗ thành công", "Tài xế {0} đã xác nhận yêu cầu của bạn."),
    BOOKING_REJECTED("Yêu cầu bị từ chối", "Rất tiếc, tài xế {0} không thể thực hiện chuyến đi này."),
    MATCH_FOUND("Tìm thấy chuyến xe phù hợp", "Tài xế {0} có chuyến đi phù hợp với bạn."),
    TRIP_STARTED("Chuyến đi bắt đầu", "Chuyến xe đi {0} đã khởi hành!"),
    TRIP_COMPLETED("Chuyến đi hoàn tất", "Chuyến đi đã kết thúc an toàn. Cảm ơn bạn đã sử dụng dịch vụ!"),
    MATCHING_IN_PROGRESS("Đang tìm kiếm tài xế", "Hệ thống đang tìm kiếm tài xế phù hợp cho bạn."),
    TRIP_CANCELED("Chuyến đi bị hủy", "Rất tiếc, chuyến đi mã #{0} đã bị hủy bởi tài xế.");

    private final String title;
    private final String bodyTemplate;

    public String formatBody(Object... args) {
        try {
            // Dùng MessageFormat thay vì String.format
            return MessageFormat.format(this.bodyTemplate, args);
        } catch (Exception e) {
            // Nếu lỗi (thiếu đối số), trả về template gốc để không làm sập hệ thống
            return this.bodyTemplate;
        }
    }
}