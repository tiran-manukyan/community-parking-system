package tir.parkingsystem.service.parking;

import tir.parkingsystem.entity.dto.request.ParkReleaseRequest;
import tir.parkingsystem.entity.dto.request.ParkRequest;
import tir.parkingsystem.entity.dto.response.ParkReleaseResponse;
import tir.parkingsystem.entity.dto.response.ParkResponse;

public interface ParkingService {

    ParkResponse park(ParkRequest request);

    ParkReleaseResponse release(ParkReleaseRequest request);
}
