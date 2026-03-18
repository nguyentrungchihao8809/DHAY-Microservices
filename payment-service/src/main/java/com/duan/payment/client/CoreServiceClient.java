package com.duan.payment.client;

import com.duan.payment.dto.CoreTripResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "core-service", url = "${app.services.core-url}")
public interface CoreServiceClient {

    @GetMapping("/api/v1/internal/bookings/{id}/payment-info")
    CoreTripResponse getBookingInfo(
            @PathVariable("id") Long bookingId,
            @RequestHeader("X-Internal-Key") String internalKey
    );
}
