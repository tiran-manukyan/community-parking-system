package tir.parkingsystem.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "parking.cost")
public class CostProperties {

    @NotNull
    @Positive
    private BigDecimal perHour;

    @NotNull
    @Positive
    private BigDecimal preBookingPerHour;

    @NotNull
    @Positive
    private BigDecimal minimal;
}