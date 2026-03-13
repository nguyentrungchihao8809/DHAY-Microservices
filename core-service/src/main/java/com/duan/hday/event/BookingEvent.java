package com.duan.hday.event;

import com.duan.hday.entity.Booking;
import lombok.Getter;

@Getter
public class BookingEvent {
    private final Booking booking;
    private final String eventType; // "CONFIRMED", "REJECTED", "TIMEOUT"

    public BookingEvent(Booking booking, String eventType) {
        this.booking = booking;
        this.eventType = eventType;
    }
}