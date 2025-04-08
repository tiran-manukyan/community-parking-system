package tir.parkingsystem.service.booking.availability;

import tir.parkingsystem.entity.dto.response.BuildingAvailableTimesResponse;

public interface ParkingAvailabilityService {

    BuildingAvailableTimesResponse getAvailableParkingTimesForBuilding(long buildingId, int days);
}
