package tir.parkingsystem.service.booking.availability;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tir.parkingsystem.entity.domain.SpotBookingEntity;
import tir.parkingsystem.entity.dto.response.BuildingAvailableTimesResponse;
import tir.parkingsystem.entity.dto.response.TimeRange;
import tir.parkingsystem.exception.ParkingIllegalStateException;
import tir.parkingsystem.repository.booking.BookingRepository;
import tir.parkingsystem.repository.building.BuildingRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingAvailabilityServiceImpl implements ParkingAvailabilityService {

    private final BookingRepository bookingRepository;
    private final BuildingRepository buildingRepository;

    @Override
    public BuildingAvailableTimesResponse getAvailableParkingTimesForBuilding(long buildingId, int days) {
        Instant from = Instant.now();
        Instant to = from.plus(days, ChronoUnit.DAYS);

        if (!buildingRepository.existsById(buildingId)) {
            throw new ParkingIllegalStateException("Building with id " + buildingId + " not found");
        }

        List<SpotBookingEntity> bookings = bookingRepository.findActiveBookingsForSpotBetween(buildingId, from, to);

        List<TimeRange> busySlots = new ArrayList<>();

        for (SpotBookingEntity booking : bookings) {
            Instant startTime = booking.getStartTime();
            Instant endTime = booking.getEndTime() == null ? to : booking.getEndTime();
            busySlots.add(new TimeRange(startTime, endTime));
        }

        List<TimeRange> mergedBusySlots = mergeTimeRanges(busySlots);

        List<TimeRange> freeSlots = new ArrayList<>();
        Instant current = from;

        for (TimeRange busy : mergedBusySlots) {
            if (current.isBefore(busy.startTime())) {
                freeSlots.add(new TimeRange(current, busy.startTime()));
            }
            if (busy.endTime().isAfter(current)) {
                current = busy.endTime();
            }
        }

        if (current.isBefore(to)) {
            freeSlots.add(new TimeRange(current, to));
        }

        return new BuildingAvailableTimesResponse(freeSlots);
    }

    private List<TimeRange> mergeTimeRanges(List<TimeRange> ranges) {
        if (ranges.isEmpty()) {
            return List.of();
        }

        ranges.sort(Comparator.comparing(TimeRange::startTime));
        List<TimeRange> merged = new ArrayList<>();
        TimeRange current = ranges.getFirst();

        for (int i = 1; i < ranges.size(); i++) {
            TimeRange next = ranges.get(i);
            if (!current.endTime().isBefore(next.startTime())) {
                current = new TimeRange(
                        current.startTime(),
                        current.endTime().isAfter(next.endTime()) ? current.endTime() : next.endTime()
                );
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return merged;
    }
}
