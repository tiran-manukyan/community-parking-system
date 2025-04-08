package tir.parkingsystem.controller.parking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tir.parkingsystem.entity.dto.request.ParkReleaseRequest;
import tir.parkingsystem.entity.dto.request.ParkRequest;
import tir.parkingsystem.entity.dto.response.ParkReleaseResponse;
import tir.parkingsystem.entity.dto.response.ParkResponse;
import tir.parkingsystem.service.parking.ParkingService;

@RestController
@RequestMapping("/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping("/park")
    public ParkResponse park(@RequestBody @NotNull @Valid ParkRequest request) {
        return parkingService.park(request);
    }

    @PostMapping("/release")
    public ParkReleaseResponse release(@RequestBody @NotNull @Valid ParkReleaseRequest request) {
        return parkingService.release(request);
    }
}
