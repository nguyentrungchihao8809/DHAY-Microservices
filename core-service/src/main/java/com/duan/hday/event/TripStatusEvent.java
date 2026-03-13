package com.duan.hday.event;

import com.duan.hday.entity.Trip;
import com.duan.hday.entity.enums.TripStatus;
import lombok.Getter;

@Getter
public class TripStatusEvent {
    private final Trip trip;
    private final TripStatus newStatus;

    public TripStatusEvent(Trip trip, TripStatus newStatus) {
        this.trip = trip;
        this.newStatus = newStatus;
    }
}
