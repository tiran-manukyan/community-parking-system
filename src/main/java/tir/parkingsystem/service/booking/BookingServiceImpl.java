package tir.parkingsystem.service.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tir.parkingsystem.entity.domain.ParkingSpotEntity;
import tir.parkingsystem.entity.domain.SpotBookingEntity;
import tir.parkingsystem.entity.dto.request.CreateBookingRequest;
import tir.parkingsystem.entity.dto.request.UpdateBookingRequest;
import tir.parkingsystem.entity.dto.response.BookingResponse;
import tir.parkingsystem.entity.dto.response.TimeRange;
import tir.parkingsystem.entity.enums.BookingStatus;
import tir.parkingsystem.exception.AvailableSpotNotFoundException;
import tir.parkingsystem.exception.BookingNotFoundException;
import tir.parkingsystem.exception.ParkingIllegalStateException;
import tir.parkingsystem.mapper.BookingMapper;
import tir.parkingsystem.properties.BookingProperties;
import tir.parkingsystem.repository.booking.BookingRepository;
import tir.parkingsystem.repository.parking.ParkingSpotRepository;
import tir.parkingsystem.service.building.BuildingService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BuildingService buildingService;
    private final ParkingSpotRepository parkingSpotRepository;
    private final BookingMapper bookingMapper;
    private final CostService costService;
    private final BookingProperties bookingProperties;

    @Override
    @Transactional
    public BookingResponse createBooking(long userId, CreateBookingRequest request) {
        validateTimeInFuture(request.startTime(), request.endTime());
        validateMinimumDuration(request.startTime(), request.endTime());

        if (!buildingService.checkUserAccessToBuilding(userId, request.buildingId())) {
            throw new ParkingIllegalStateException("User does not have access to the building");
        }

        ParkingSpotEntity spot = findFirstAvailableSpot(request.buildingId(), request.startTime(), request.endTime());

        BigDecimal parkingCost;
        if (request.endTime() != null) {
            parkingCost = costService.calculateParkingCost(new TimeRange(request.startTime(), request.endTime()), true);
        } else {
            parkingCost = BigDecimal.ZERO;
        }

        SpotBookingEntity booking = bookingMapper.toEntity(userId, request, spot.getId(), parkingCost);

        bookingRepository.save(booking);

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(long userId, UpdateBookingRequest request) {
        validateTimeInFuture(request.startTime(), request.endTime());
        validateMinimumDuration(request.startTime(), request.endTime());

        SpotBookingEntity spotBooking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new BookingNotFoundException(request.bookingId()));

        if (spotBooking.getUserId() != userId) {
            throw new BookingNotFoundException(request.bookingId());
        }

        if (spotBooking.getStatus() != BookingStatus.BOOKED) {
            throw new ParkingIllegalStateException("Booking cannot be updated");
        }

        ParkingSpotEntity newSpot = findFirstAvailableSpot(spotBooking.getSpot().getBuildingId(), request.startTime(), request.endTime());

        spotBooking.setStartTime(request.startTime());
        spotBooking.setEndTime(request.endTime());
        spotBooking.setSpotId(newSpot.getId());

        SpotBookingEntity updatedBooking = bookingRepository.save(spotBooking);

        return bookingMapper.toResponse(updatedBooking);
    }

    @Override
    public void cancelBooking(long userId, long bookingId) {
        SpotBookingEntity spotBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (spotBooking.getUserId() != userId) {
            throw new BookingNotFoundException(bookingId);
        }

        if (spotBooking.getStatus() != BookingStatus.BOOKED) {
            throw new ParkingIllegalStateException("Booking cannot be cancelled");
        }

        spotBooking.setStatus(BookingStatus.CANCELLED);
        spotBooking.setEndTime(Instant.now());

        bookingRepository.save(spotBooking);
    }

    @Override
    public BookingResponse getBookingById(long userId, long bookingId) {
        SpotBookingEntity spotBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (spotBooking.getUserId() != userId) {
            throw new BookingNotFoundException(bookingId);
        }

        return bookingMapper.toResponse(spotBooking);
    }

    @Override
    public List<BookingResponse> getAllBookingsByUserId(long userId) {
        List<SpotBookingEntity> bookings = bookingRepository.findByUserId(
                userId, Sort.by(Sort.Order.asc("startTime"))
        );

        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    private ParkingSpotEntity findFirstAvailableSpot(long buildingId, Instant startTime, Instant endTime) {
        List<ParkingSpotEntity> spots = parkingSpotRepository.findAvailableInBuilding(buildingId,
                startTime, endTime, PageRequest.of(0, 1));
        if (spots.isEmpty()) {
            throw new AvailableSpotNotFoundException(buildingId);
        }
        return spots.getFirst();
    }

    private void validateMinimumDuration(Instant start, Instant end) {
        if (end != null) {
            if (Duration.between(start, end).compareTo(bookingProperties.getMinimumDuration()) < 0) {
                throw new ParkingIllegalStateException("Booking duration must be at least " + bookingProperties.getMinimumDuration() + " minutes.");
            }
        }
    }

    private void validateTimeInFuture(Instant start, Instant end) {
        Instant now = Instant.now();

        if (start.plusSeconds(1).isBefore(now)) {
            throw new ParkingIllegalStateException("Start time must be in the future.");
        }

        if (end != null && end.isBefore(now)) {
            throw new ParkingIllegalStateException("End time must be in the future.");
        }
    }
}
