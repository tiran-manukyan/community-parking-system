package tir.parkingsystem.entity.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkRequest {
    @NotNull
    private Long userId;
    @Nullable
    private Long buildingId;
    @Nullable
    private Long bookingId;
    @NotBlank
    private String plateNumber;
}