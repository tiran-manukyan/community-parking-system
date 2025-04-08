package tir.parkingsystem.service.building;

import tir.parkingsystem.entity.dto.response.BuildingResponse;

import java.util.List;

public interface BuildingService {

    List<BuildingResponse> getAllAccessibleBuildings(long userId, int days);

    boolean checkUserAccessToBuilding(long userId, long buildingId);
}
