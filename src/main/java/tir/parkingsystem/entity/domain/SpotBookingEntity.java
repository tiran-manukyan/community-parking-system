package tir.parkingsystem.entity.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tir.parkingsystem.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "spot_booking", schema = "parking_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotBookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(name = "spot_id")
    private Long spotId;

    private Instant startTime;

    private Instant endTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private BigDecimal parkingCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", insertable = false, updatable = false)
    private ParkingSpotEntity spot;
}

