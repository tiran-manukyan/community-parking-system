package tir.parkingsystem.controller.aviability;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tir.parkingsystem.entity.dto.response.BuildingAvailableTimesResponse;
import tir.parkingsystem.service.booking.availability.ParkingAvailabilityService;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
public class ParkingAvailabilityController {

    private final ParkingAvailabilityService availabilityService;

    @GetMapping("/building/{buildingId}")
    public BuildingAvailableTimesResponse getAvailableTimes(
            @PathVariable("buildingId") long buildingId,
            @RequestParam(value = "days", required = false, defaultValue = "1") int days) {
        return availabilityService.getAvailableParkingTimesForBuilding(buildingId, days);
    }
}
