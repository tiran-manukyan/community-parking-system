package tir.parkingsystem.service.building;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tir.parkingsystem.entity.dto.response.BuildingResponse;
import tir.parkingsystem.repository.building.BuildingRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;

    @Override
    public List<BuildingResponse> getAllAccessibleBuildings(long userId, int days) {
        List<Long> buildingsWithPublicParkingSpots = buildingRepository.findBuildingsWithPublicParkingSpots();
        List<Long> buildingsWithAccessibleParkingSpots = buildingRepository.findBuildingsByUserId(userId);

        Set<Long> buildingIds = Stream.concat(buildingsWithPublicParkingSpots.stream(), buildingsWithAccessibleParkingSpots.stream())
                .collect(Collectors.toSet());

        return buildingRepository.findBuildingsWithAvailableSpots(buildingIds, calculateTimeLimit(days));
    }

    @Override
    public boolean checkUserAccessToBuilding(long userId, long buildingId) {
        return buildingRepository.hasAccessToBuilding(userId, buildingId);
    }

    private Instant calculateTimeLimit(long days) {
        return Instant.now().plus(days, ChronoUnit.DAYS);
    }
}
