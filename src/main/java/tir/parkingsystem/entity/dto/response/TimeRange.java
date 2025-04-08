package tir.parkingsystem.entity.dto.response;

import java.time.Instant;

public record TimeRange(
        Instant startTime,
        Instant endTime
) {

    public TimeRange(Instant startTime, Instant endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time is after end time");
        }
    }
}
