package tir.parkingsystem.controller.building;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tir.parkingsystem.entity.dto.response.BuildingResponse;
import tir.parkingsystem.security.UserContext;
import tir.parkingsystem.service.building.BuildingService;

import java.util.List;

@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;
    private final UserContext userContext;

    @GetMapping
    public ResponseEntity<List<BuildingResponse>> getAllAccessibleBuildings(
            @RequestParam(value = "days", required = false, defaultValue = "1") int days) {
        long userId = userContext.getCurrentUserId();

        List<BuildingResponse> response = buildingService.getAllAccessibleBuildings(userId, days);
        return ResponseEntity.ok(response);
    }
}
