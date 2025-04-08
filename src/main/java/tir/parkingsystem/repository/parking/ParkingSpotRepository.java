package tir.parkingsystem.repository.parking;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import tir.parkingsystem.entity.domain.ParkingSpotEntity;

import java.time.Instant;
import java.util.List;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpotEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT spot FROM ParkingSpotEntity spot
            LEFT JOIN SpotBookingEntity booking ON booking.spotId = spot.id
                        AND ((:startTime >= booking.startTime AND :startTime <= booking.endTime)
                                    OR CAST(:endTime AS TIMESTAMP) IS NULL 
                                    OR (:endTime >= booking.startTime AND :endTime <= booking.endTime))
                        AND (booking.status = 'BOOKED' OR booking.status = 'USED')
            WHERE spot.buildingId = :buildingId AND booking.id IS NULL
            """)
    List<ParkingSpotEntity> findAvailableInBuilding(long buildingId, Instant startTime, Instant endTime, Pageable pageable);

}