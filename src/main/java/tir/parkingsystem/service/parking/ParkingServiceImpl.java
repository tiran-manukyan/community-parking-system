package tir.parkingsystem.service.parking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tir.parkingsystem.entity.domain.ParkingSessionEntity;
import tir.parkingsystem.entity.domain.SpotBookingEntity;
import tir.parkingsystem.entity.dto.request.CreateBookingRequest;
import tir.parkingsystem.entity.dto.request.ParkReleaseRequest;
import tir.parkingsystem.entity.dto.request.ParkRequest;
import tir.parkingsystem.entity.dto.response.BookingResponse;
import tir.parkingsystem.entity.dto.response.ParkReleaseResponse;
import tir.parkingsystem.entity.dto.response.ParkResponse;
import tir.parkingsystem.entity.dto.response.TimeRange;
import tir.parkingsystem.entity.enums.BookingStatus;
import tir.parkingsystem.exception.BookingNotFoundException;
import tir.parkingsystem.exception.ParkingIllegalStateException;
import tir.parkingsystem.repository.booking.BookingRepository;
import tir.parkingsystem.repository.parking.ParkingSessionRepository;
import tir.parkingsystem.service.booking.BookingService;
import tir.parkingsystem.service.booking.CostService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private final ParkingSessionRepository parkingSessionRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final CostService costService;

    @Override
    @Transactional
    public ParkResponse park(ParkRequest request) {
        Optional<SpotBookingEntity> bookingOpt;
        boolean isPreBooked;
        if (Objects.isNull(request.getBookingId())) {
            BookingResponse response = bookingService.createBooking(request.getUserId(), new CreateBookingRequest(request.getBuildingId(), Instant.now(), null));
            bookingOpt = bookingRepository.findById(response.id());
            isPreBooked = false;
        } else {
            bookingOpt = bookingRepository.findById(request.getBookingId());
            isPreBooked = true;
        }

        SpotBookingEntity booking = bookingOpt.orElseThrow(() -> new BookingNotFoundException(request.getBookingId()));
        booking.setStatus(BookingStatus.USED);

        ParkingSessionEntity sessionEntity = ParkingSessionEntity.builder()
                .userId(request.getUserId())
                .spotId(booking.getSpotId())
                .bookingId(booking.getId())
                .startTime(booking.getStartTime())
                .plateNumber(request.getPlateNumber())
                .isPreBooked(isPreBooked)
                .build();

        parkingSessionRepository.save(sessionEntity);

        return new ParkResponse(booking.getSpotId(), sessionEntity.getId());
    }

    @Override
    @Transactional
    public ParkReleaseResponse release(ParkReleaseRequest request) {
        ParkingSessionEntity sessionEntity = parkingSessionRepository.findById(request.getSessionId()).orElseThrow();

        if (Objects.nonNull(sessionEntity.getEndTime())) {
            throw new ParkingIllegalStateException("The session is already ended");
        }

        Instant endTime = Instant.now();
        sessionEntity.setEndTime(endTime);

        SpotBookingEntity booking = bookingRepository.findById(sessionEntity.getBookingId()).orElseThrow();
        if (!sessionEntity.getIsPreBooked()) {
            booking.setStatus(BookingStatus.COMPLETED);
        }

        if (Objects.nonNull(booking.getEndTime())) {
            if (booking.getEndTime().isBefore(endTime)) {
                booking.setStatus(BookingStatus.COMPLETED);
            }

            return new ParkReleaseResponse(BigDecimal.ZERO);
        }

        BigDecimal bigDecimal = costService.calculateParkingCost(
                new TimeRange(sessionEntity.getStartTime(), endTime),
                sessionEntity.getIsPreBooked()
        );

        return new ParkReleaseResponse(bigDecimal);
    }
}
