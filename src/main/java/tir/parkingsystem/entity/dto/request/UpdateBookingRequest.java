package tir.parkingsystem.entity.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateBookingRequest(
        @NotNull Long bookingId,
        @NotNull Instant startTime,
        @Nullable Instant endTime
) {
}
