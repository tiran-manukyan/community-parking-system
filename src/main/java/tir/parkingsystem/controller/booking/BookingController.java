package tir.parkingsystem.controller.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tir.parkingsystem.entity.dto.request.CreateBookingRequest;
import tir.parkingsystem.entity.dto.request.UpdateBookingRequest;
import tir.parkingsystem.entity.dto.response.BookingResponse;
import tir.parkingsystem.security.UserContext;
import tir.parkingsystem.service.booking.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final UserContext userContext;
    private final BookingService bookingService;

    @PostMapping
    public BookingResponse createBooking(@RequestBody @NotNull @Valid CreateBookingRequest request) {
        long userId = userContext.getCurrentUserId();
        return bookingService.createBooking(userId, request);
    }

    @PutMapping("/update")
    public BookingResponse updateBooking(@RequestBody @NotNull @Valid UpdateBookingRequest request) {
        long userId = userContext.getCurrentUserId();
        return bookingService.updateBooking(userId, request);
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable @NotNull Long bookingId) {
        long userId = userContext.getCurrentUserId();
        bookingService.cancelBooking(userId, bookingId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(@PathVariable @NotNull Long bookingId) {
        long userId = userContext.getCurrentUserId();

        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponse> getAllBookings() {
        long userId = userContext.getCurrentUserId();

        return bookingService.getAllBookingsByUserId(userId);
    }
}
