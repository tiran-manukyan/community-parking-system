package tir.parkingsystem.repository.building;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tir.parkingsystem.entity.domain.BuildingEntity;
import tir.parkingsystem.entity.dto.response.BuildingResponse;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public interface BuildingRepository extends JpaRepository<BuildingEntity, Long> {

    @Query("""
            SELECT b.id FROM BuildingEntity b
            JOIN b.communities c
            JOIN c.users u
            WHERE u.id = :userId
            """)
    List<Long> findBuildingsByUserId(long userId);

    @Query("SELECT b.id FROM BuildingEntity b JOIN b.parkingSpots ps WHERE ps.ownerCommunityId IS NULL")
    List<Long> findBuildingsWithPublicParkingSpots();

    @Query("""
                SELECT new tir.parkingsystem.entity.dto.response.BuildingResponse(
                    b.id,
                    b.name,
                    b.address,
                    COUNT(DISTINCT ps.id)
                )
                FROM BuildingEntity b
                JOIN b.parkingSpots ps
                WHERE b.id IN :buildingIds
                AND NOT EXISTS (
                    SELECT 1 FROM SpotBookingEntity sb
                    WHERE sb.spotId = ps.id
                    AND sb.status = 'BOOKED'
                    AND (sb.endTime IS NULL OR sb.endTime > CURRENT_TIMESTAMP)
                    AND sb.startTime < :timeLimit
                )
                AND NOT EXISTS (
                    SELECT 1 FROM ParkingSessionEntity session
                    WHERE session.spotId = ps.id
                    AND session.endTime IS NULL
                )
                GROUP BY b.id, b.name
                HAVING COUNT(DISTINCT ps.id) > 0
            """)
    List<BuildingResponse> findBuildingsWithAvailableSpots(@Param("buildingIds") Set<Long> buildingIds,
                                                           @Param("timeLimit") Instant timeLimit);

    @Query(value = """
            SELECT EXISTS (SELECT 1
                           FROM parking_system.parking_spot ps
                           WHERE ps.building_id = :buildingId
                             AND (
                               ps.owner_community_id IS NULL
                                   OR ps.owner_community_id IN (SELECT uc.community_id
                                                                FROM parking_system.users u
                                                                         JOIN parking_system.users_communities uc
                                                                              ON u.id = uc.user_id
                                                                WHERE u.id = :userId)
                               ))
            """, nativeQuery = true)
    boolean hasAccessToBuilding(@Param("userId") long userId, @Param("buildingId") long buildingId);

}

