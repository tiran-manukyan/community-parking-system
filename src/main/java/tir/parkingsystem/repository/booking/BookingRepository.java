package tir.parkingsystem.repository.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tir.parkingsystem.entity.domain.SpotBookingEntity;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<SpotBookingEntity, Long> {

    List<SpotBookingEntity> findByUserId(Long userId, Sort sort);

    @Query("""
                SELECT sb FROM SpotBookingEntity sb
                WHERE sb.spot.buildingId = :buildingId
                  AND (sb.status = 'BOOKED' OR sb.status = 'USED')
                  AND sb.endTime > :start
                  AND sb.startTime < :end
            """)
    List<SpotBookingEntity> findActiveBookingsForSpotBetween(
            @Param("buildingId") long buildingId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

}
