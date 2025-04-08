package tir.parkingsystem.repository.parking;

import org.springframework.data.jpa.repository.JpaRepository;
import tir.parkingsystem.entity.domain.ParkingSessionEntity;

public interface ParkingSessionRepository extends JpaRepository<ParkingSessionEntity, Long> {
}