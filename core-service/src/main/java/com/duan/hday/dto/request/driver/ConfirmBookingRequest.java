package com.duan.hday.dto.request.driver;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter @Setter
public class ConfirmBookingRequest {
    @NotNull(message = "Trip ID không được để trống")
    private Long tripId;

    @NotNull(message = "Request ID của hành khách không được để trống")
    private Long passengerRequestId;
}
