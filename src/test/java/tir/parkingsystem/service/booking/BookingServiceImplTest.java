package tir.parkingsystem.service.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final long USER_ID = 1L;
    private static final long SPOT_ID = 3L;
    private static final long BOOKING_ID = 3L;
    private static final long BUILDING_ID = 4L;

    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BuildingService buildingService;

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CostService costService;

    @Mock
    private BookingProperties bookingProperties;

    private SpotBookingEntity bookingEntity;
    private ParkingSpotEntity spotEntity;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository, buildingService, parkingSpotRepository, bookingMapper, costService, bookingProperties
        );

        spotEntity = new ParkingSpotEntity();
        spotEntity.setId(SPOT_ID);
        spotEntity.setBuildingId(BUILDING_ID);

        bookingEntity = new SpotBookingEntity();
        bookingEntity.setId(BOOKING_ID);
        bookingEntity.setUserId(USER_ID);
        bookingEntity.setStatus(BookingStatus.BOOKED);
        bookingEntity.setStartTime(Instant.now());
        bookingEntity.setEndTime(Instant.now().plusSeconds(3600));
        bookingEntity.setSpot(spotEntity);
    }

    @Test
    public void createBooking_endTimeIsNotNull_success() {
        // Given
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        CreateBookingRequest request = new CreateBookingRequest(BUILDING_ID, start, end);

        BigDecimal parkingPrice = BigDecimal.TEN;

        when(buildingService.checkUserAccessToBuilding(USER_ID, request.buildingId())).thenReturn(true);
        when(parkingSpotRepository.findAvailableInBuilding(
                request.buildingId(),
                request.startTime(),
                request.endTime(),
                PageRequest.of(0, 1))
        ).thenReturn(List.of(spotEntity));
        when(costService.calculateParkingCost(
                argThat(timeRange -> timeRange.startTime() == request.startTime()
                        && timeRange.endTime() == request.endTime()),
                eq(true))
        ).thenReturn(parkingPrice);
        when(bookingMapper.toEntity(USER_ID, request, spotEntity.getId(), parkingPrice)).thenReturn(bookingEntity);
        when(bookingRepository.save(bookingEntity)).thenReturn(bookingEntity);
        when(bookingMapper.toResponse(bookingEntity)).thenReturn(mock(BookingResponse.class));

        // When
        BookingResponse response = bookingService.createBooking(USER_ID, request);

        // Then
        assertNotNull(response);
        verify(buildingService).checkUserAccessToBuilding(USER_ID, request.buildingId());
        verify(parkingSpotRepository).findAvailableInBuilding(
                request.buildingId(),
                request.startTime(),
                request.endTime(),
                PageRequest.of(0, 1)
        );
        verify(costService).calculateParkingCost(
                argThat(timeRange -> timeRange.startTime() == request.startTime()
                        && timeRange.endTime() == request.endTime()),
                eq(true)
        );
        verify(bookingMapper).toEntity(USER_ID, request, spotEntity.getId(), parkingPrice);
        verify(bookingRepository).save(bookingEntity);
        verify(bookingMapper).toResponse(bookingEntity);
    }

    @Test
    public void createBooking_endTimeIsNull_success() {
        // Given
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        CreateBookingRequest request = new CreateBookingRequest(BUILDING_ID, start, null);

        when(buildingService.checkUserAccessToBuilding(USER_ID, request.buildingId())).thenReturn(true);
        when(parkingSpotRepository.findAvailableInBuilding(
                request.buildingId(),
                request.startTime(),
                request.endTime(),
                PageRequest.of(0, 1))
        ).thenReturn(List.of(spotEntity));
        when(bookingMapper.toEntity(USER_ID, request, spotEntity.getId(), BigDecimal.ZERO)).thenReturn(bookingEntity);
        when(bookingRepository.save(bookingEntity)).thenReturn(bookingEntity);
        when(bookingMapper.toResponse(bookingEntity)).thenReturn(mock(BookingResponse.class));

        // When
        BookingResponse response = bookingService.createBooking(USER_ID, request);

        // Then
        assertNotNull(response);
        verify(buildingService).checkUserAccessToBuilding(USER_ID, request.buildingId());
        verify(parkingSpotRepository).findAvailableInBuilding(
                request.buildingId(),
                request.startTime(),
                request.endTime(),
                PageRequest.of(0, 1)
        );
        verify(costService, never()).calculateParkingCost(any(TimeRange.class), anyBoolean());
        verify(bookingMapper).toEntity(USER_ID, request, spotEntity.getId(), BigDecimal.ZERO);
        verify(bookingRepository).save(bookingEntity);
        verify(bookingMapper).toResponse(bookingEntity);
    }

    @Test
    public void createBooking_bookingStartTImeIsNotInFuture_exception() {
        // Given
        Instant start = Instant.now().minus(1, ChronoUnit.MINUTES);
        CreateBookingRequest request = new CreateBookingRequest(BUILDING_ID, start, null);

        // When
        ParkingIllegalStateException exception = assertThrows(
                ParkingIllegalStateException.class, () -> bookingService.createBooking(USER_ID, request)
        );

        // Then
        assertEquals("Start time must be in the future.", exception.getMessage());
        verify(buildingService, never()).checkUserAccessToBuilding(anyLong(), anyLong());
        verify(parkingSpotRepository, never()).findAvailableInBuilding(anyLong(), any(), any(), any());
        verify(costService, never()).calculateParkingCost(any(TimeRange.class), anyBoolean());
        verify(bookingMapper, never()).toEntity(anyLong(), any(), anyLong(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void createBooking_bookingEndTImeIsNotInFuture_exception() {
        // Given
        Instant start = Instant.now();
        Instant end = start.minus(1, ChronoUnit.HOURS);
        CreateBookingRequest request = new CreateBookingRequest(BUILDING_ID, start, end);

        // When
        ParkingIllegalStateException exception = assertThrows(
                ParkingIllegalStateException.class, () -> bookingService.createBooking(USER_ID, request)
        );

        // Then
        assertEquals("End time must be in the future.", exception.getMessage());
        verify(buildingService, never()).checkUserAccessToBuilding(anyLong(), anyLong());
        verify(parkingSpotRepository, never()).findAvailableInBuilding(anyLong(), any(), any(), any());
        verify(costService, never()).calculateParkingCost(any(TimeRange.class), anyBoolean());
        verify(bookingMapper, never()).toEntity(anyLong(), any(), anyLong(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void createBooking_bookingMinimalTimeMustBeAnHour_exception() {
        // Given
        Duration minimumDuration = Duration.ofMinutes(60);

        Instant start = Instant.now().plus(1, ChronoUnit.MINUTES);
        Instant end = start.plus(30, ChronoUnit.MINUTES);
        CreateBookingRequest request = new CreateBookingRequest(BUILDING_ID, start, end);

        when(bookingProperties.getMinimumDuration()).thenReturn(minimumDuration);

        // When
        ParkingIllegalStateException exception = assertThrows(
                ParkingIllegalStateException.class, () -> bookingService.createBooking(USER_ID, request)
        );

        // Then
        assertEquals(String.format("Booking duration must be at least %s minutes.", minimumDuration), exception.getMessage());
        verify(buildingService, never()).checkUserAccessToBuilding(anyLong(), anyLong());
        verify(parkingSpotRepository, never()).findAvailableInBuilding(anyLong(), any(), any(), any());
        verify(costService, never()).calculateParkingCost(any(TimeRange.class), anyBoolean());
        verify(bookingMapper, never()).toEntity(anyLong(), any(), anyLong(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void createBooking_userDoesNotHaveAccessToBuilding_exception() {
        // Given
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        CreateBookingRequest request = new CreateBookingRequest(BUILDING_ID, start, null);

        when(buildingService.checkUserAccessToBuilding(USER_ID, request.buildingId())).thenReturn(false);

        // When
        ParkingIllegalStateException exception = assertThrows(
                ParkingIllegalStateException.class, () -> bookingService.createBooking(USER_ID, request)
        );

        // Then
        assertEquals("User does not have access to the building", exception.getMessage());
        verify(buildingService).checkUserAccessToBuilding(USER_ID, request.buildingId());
        verify(parkingSpotRepository, never()).findAvailableInBuilding(anyLong(), any(), any(), any());
        verify(costService, never()).calculateParkingCost(any(TimeRange.class), anyBoolean());
        verify(bookingMapper, never()).toEntity(anyLong(), any(), anyLong(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void createBooking_availableSpotNotFound_exception() {
        // Given
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        CreateBookingRequest request = new CreateBookingRequest(BUILDING_ID, start, null);

        when(buildingService.checkUserAccessToBuilding(USER_ID, request.buildingId())).thenReturn(true);
        when(parkingSpotRepository.findAvailableInBuilding(
                request.buildingId(),
                request.startTime(),
                request.endTime(),
                PageRequest.of(0, 1))
        ).thenReturn(Collections.emptyList());

        // When
        AvailableSpotNotFoundException exception = assertThrows(
                AvailableSpotNotFoundException.class, () -> bookingService.createBooking(USER_ID, request)
        );

        // Then
        assertEquals(String.format("Available spot in building %d not found", request.buildingId()), exception.getMessage());
        verify(buildingService).checkUserAccessToBuilding(USER_ID, request.buildingId());
        verify(parkingSpotRepository).findAvailableInBuilding(
                request.buildingId(),
                request.startTime(),
                request.endTime(),
                PageRequest.of(0, 1)
        );
        verify(costService, never()).calculateParkingCost(any(TimeRange.class), anyBoolean());
        verify(bookingMapper, never()).toEntity(anyLong(), any(), anyLong(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void updateBooking_success() {
        // Given
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        UpdateBookingRequest request = new UpdateBookingRequest(BOOKING_ID, start, end);

        when(bookingRepository.findById(request.bookingId())).thenReturn(Optional.of(bookingEntity));
        when(parkingSpotRepository.findAvailableInBuilding(
                bookingEntity.getSpot().getBuildingId(),
                request.startTime(),
                request.endTime(),
                PageRequest.of(0, 1))
        ).thenReturn(List.of(spotEntity));

        when(bookingRepository.save(bookingEntity)).thenReturn(bookingEntity);
        when(bookingMapper.toResponse(bookingEntity)).thenReturn(mock(BookingResponse.class));

        // When
        BookingResponse response = bookingService.updateBooking(USER_ID, request);

        // Then
        assertNotNull(response);
        verify(bookingRepository).findById(request.bookingId());
        verify(parkingSpotRepository).findAvailableInBuilding(
                bookingEntity.getSpot().getBuildingId(),
                request.startTime(),
                request.endTime(),
                PageRequest.of(0, 1)
        );
        verify(bookingRepository).save(bookingEntity);
        verify(bookingMapper).toResponse(bookingEntity);
    }

    @Test
    public void updateBooking_bookingStartTImeIsNotInFuture_exception() {
        // Given
        Instant start = Instant.now().minus(1, ChronoUnit.MINUTES);
        UpdateBookingRequest request = new UpdateBookingRequest(BUILDING_ID, start, null);

        // When
        ParkingIllegalStateException exception = assertThrows(
                ParkingIllegalStateException.class, () -> bookingService.updateBooking(USER_ID, request)
        );

        // Then
        assertEquals("Start time must be in the future.", exception.getMessage());
        verify(bookingRepository, never()).findById(anyLong());
        verify(parkingSpotRepository, never()).findAvailableInBuilding(anyLong(), any(), any(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void updateBooking_bookingEndTImeIsNotInFuture_exception() {
        // Given
        Instant start = Instant.now();
        Instant end = start.minus(1, ChronoUnit.HOURS);
        UpdateBookingRequest request = new UpdateBookingRequest(BUILDING_ID, start, end);

        // When
        ParkingIllegalStateException exception = assertThrows(
                ParkingIllegalStateException.class, () -> bookingService.updateBooking(USER_ID, request)
        );

        // Then
        assertEquals("End time must be in the future.", exception.getMessage());
        verify(bookingRepository, never()).findById(anyLong());
        verify(parkingSpotRepository, never()).findAvailableInBuilding(anyLong(), any(), any(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void updateBooking_bookingNotFound_exception() {
        // Given
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        UpdateBookingRequest request = new UpdateBookingRequest(BOOKING_ID, start, null);

        when(bookingRepository.findById(request.bookingId())).thenReturn(Optional.empty());

        // When
        BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class, () -> bookingService.updateBooking(USER_ID, request)
        );

        // Then
        assertEquals(String.format("Booking with ID %d not found", request.bookingId()), exception.getMessage());
        verify(bookingRepository).findById(request.bookingId());
        verify(parkingSpotRepository, never()).findAvailableInBuilding(anyLong(), any(), any(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void updateBooking_bookingNotFound_bookingBelongsToAnotherUser_exception() {
        // Given
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        UpdateBookingRequest request = new UpdateBookingRequest(BOOKING_ID, start, null);

        bookingEntity.setUserId(77L);

        when(bookingRepository.findById(request.bookingId())).thenReturn(Optional.of(bookingEntity));

        // When
        BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class, () -> bookingService.updateBooking(USER_ID, request)
        );

        // Then
        assertEquals(String.format("Booking with ID %d not found", request.bookingId()), exception.getMessage());
        verify(bookingRepository).findById(request.bookingId());
        verify(parkingSpotRepository, never()).findAvailableInBuilding(anyLong(), any(), any(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @ParameterizedTest
    @MethodSource("provideBookingStatusesExcludingBooked")
    public void updateBooking_bookingCannotBeUpdated_statusIsNotBooked_exception(BookingStatus bookingStatus) {
        // Given
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        UpdateBookingRequest request = new UpdateBookingRequest(BOOKING_ID, start, null);

        bookingEntity.setStatus(bookingStatus);

        when(bookingRepository.findById(request.bookingId())).thenReturn(Optional.of(bookingEntity));

        // When
        ParkingIllegalStateException exception = assertThrows(
                ParkingIllegalStateException.class, () -> bookingService.updateBooking(USER_ID, request)
        );

        // Then
        assertEquals("Booking cannot be updated", exception.getMessage());
        verify(bookingRepository).findById(request.bookingId());
        verify(parkingSpotRepository, never()).findAvailableInBuilding(anyLong(), any(), any(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void updateBooking_availableSpotNotFound_exception() {
        // Given
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        UpdateBookingRequest request = new UpdateBookingRequest(BOOKING_ID, start, end);

        when(bookingRepository.findById(request.bookingId())).thenReturn(Optional.of(bookingEntity));
        when(parkingSpotRepository.findAvailableInBuilding(
                bookingEntity.getSpot().getBuildingId(),
                request.startTime(),
                request.endTime(),
                PageRequest.of(0, 1))
        ).thenReturn(Collections.emptyList());

        // When
        AvailableSpotNotFoundException exception = assertThrows(
                AvailableSpotNotFoundException.class, () -> bookingService.updateBooking(USER_ID, request)
        );

        // Then
        assertEquals(String.format("Available spot in building %d not found", bookingEntity.getSpot().getBuildingId()), exception.getMessage());
        verify(bookingRepository).findById(request.bookingId());
        verify(parkingSpotRepository).findAvailableInBuilding(
                bookingEntity.getSpot().getBuildingId(),
                request.startTime(),
                request.endTime(),
                PageRequest.of(0, 1)
        );
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void cancelBooking_success() {
        // Given
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(bookingEntity));

        // When
        bookingService.cancelBooking(USER_ID, BOOKING_ID);

        // Then
        verify(bookingRepository).findById(BOOKING_ID);

        ArgumentCaptor<SpotBookingEntity> captor = ArgumentCaptor.forClass(SpotBookingEntity.class);
        verify(bookingRepository).save(captor.capture());

        SpotBookingEntity saved = captor.getValue();

        assertEquals(BookingStatus.CANCELLED, saved.getStatus());
        assertNotNull(saved.getEndTime());

        // Optionally: ensure endTime was set to roughly now
        assertTrue(Duration.between(saved.getEndTime(), Instant.now()).abs().getSeconds() < 2);
    }

    @Test
    public void cancelBooking_bookingNotFound_exception() {
        // Given
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        // When
        BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class, () -> bookingService.cancelBooking(USER_ID, BOOKING_ID)
        );

        // Then
        assertEquals(String.format("Booking with ID %d not found", BOOKING_ID), exception.getMessage());
        verify(bookingRepository).findById(BOOKING_ID);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    public void cancelBooking_bookingNotFound_bookingBelongsToAnotherUser_exception() {
        // Given
        bookingEntity.setUserId(33L);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(bookingEntity));

        // When
        BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class, () -> bookingService.cancelBooking(USER_ID, BOOKING_ID)
        );

        // Then
        assertEquals(String.format("Booking with ID %d not found", BOOKING_ID), exception.getMessage());
        verify(bookingRepository).findById(BOOKING_ID);
        verify(bookingRepository, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("provideBookingStatusesExcludingBooked")
    public void cancelBooking_bookingCannotBeUpdated_statusIsNotBooked_exception(BookingStatus bookingStatus) {
        // Given
        bookingEntity.setStatus(bookingStatus);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(bookingEntity));

        // When
        ParkingIllegalStateException exception = assertThrows(
                ParkingIllegalStateException.class, () -> bookingService.cancelBooking(USER_ID, BOOKING_ID)
        );

        // Then
        assertEquals("Booking cannot be cancelled", exception.getMessage());
        verify(bookingRepository).findById(BOOKING_ID);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    public void getBookingById_success() {
        // Given
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(bookingEntity));
        when(bookingMapper.toResponse(bookingEntity)).thenReturn(mock(BookingResponse.class));

        // When
        BookingResponse booking = bookingService.getBookingById(USER_ID, BOOKING_ID);

        // Then
        assertNotNull(booking);
        verify(bookingRepository).findById(BOOKING_ID);
        verify(bookingMapper).toResponse(bookingEntity);
    }

    @Test
    public void getBookingById_bookingNotFound_exception() {
        // Given
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        // When
        BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class, () -> bookingService.getBookingById(USER_ID, BOOKING_ID)
        );

        // Then
        assertEquals(String.format("Booking with ID %d not found", BOOKING_ID), exception.getMessage());
        verify(bookingRepository).findById(BOOKING_ID);
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void getBookingById_bookingNotFound_bookingBelongsToAnotherUser_exception() {
        // Given
        bookingEntity.setUserId(22L);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(bookingEntity));

        // When
        BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class, () -> bookingService.getBookingById(USER_ID, BOOKING_ID)
        );

        // Then
        assertEquals(String.format("Booking with ID %d not found", BOOKING_ID), exception.getMessage());
        verify(bookingRepository).findById(BOOKING_ID);
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    public void getAllBookingsByUserId_success() {
        // Given
        when(bookingRepository.findByUserId(USER_ID, Sort.by(Sort.Order.asc("startTime"))))
                .thenReturn(List.of(bookingEntity));
        when(bookingMapper.toResponse(bookingEntity)).thenReturn(mock(BookingResponse.class));

        // When
        List<BookingResponse> bookings = bookingService.getAllBookingsByUserId(USER_ID);

        // Then
        assertNotNull(bookings);
        verify(bookingRepository).findByUserId(USER_ID, Sort.by(Sort.Order.asc("startTime")));
        verify(bookingMapper).toResponse(bookingEntity);
    }

    private static Stream<BookingStatus> provideBookingStatusesExcludingBooked() {
        return Stream.of(BookingStatus.USED, BookingStatus.CANCELLED, BookingStatus.COMPLETED);
    }
}
