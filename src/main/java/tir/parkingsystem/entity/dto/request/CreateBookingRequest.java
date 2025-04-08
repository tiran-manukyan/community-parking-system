package tir.parkingsystem.entity.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateBookingRequest(
        @NotNull Long buildingId,
        @NotNull Instant startTime,
        @Nullable Instant endTime
) {
}
