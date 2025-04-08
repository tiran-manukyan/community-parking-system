package tir.parkingsystem.entity.dto.response;

import tir.parkingsystem.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record BookingResponse(
        Long id,
        Long spotId,
        Instant startTime,
        Instant endTime,
        BigDecimal parkingCost,
        BookingStatus status
) {
}
