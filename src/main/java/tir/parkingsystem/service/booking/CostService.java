package tir.parkingsystem.service.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tir.parkingsystem.entity.dto.response.TimeRange;
import tir.parkingsystem.properties.CostProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CostService {

    private static final BigDecimal _60 = BigDecimal.valueOf(60);

    private final CostProperties costProperties;

    public BigDecimal calculateParkingCost(TimeRange timeRange, boolean isPreBooked) {
        Instant startTime = timeRange.startTime();
        Instant endTime = timeRange.endTime();
        Duration duration = Duration.between(startTime, endTime);
        BigDecimal minutes = BigDecimal.valueOf(duration.toMinutes());

        BigDecimal hourCost = isPreBooked
                ? costProperties.getPreBookingPerHour()
                : costProperties.getPerHour();

        return hourCost.divide(_60, RoundingMode.UP).multiply(minutes).max(costProperties.getMinimal());
    }

}
