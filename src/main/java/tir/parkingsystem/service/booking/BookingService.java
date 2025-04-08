package tir.parkingsystem.service.booking;


import tir.parkingsystem.entity.dto.request.CreateBookingRequest;
import tir.parkingsystem.entity.dto.request.UpdateBookingRequest;
import tir.parkingsystem.entity.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(long userId, CreateBookingRequest request);

    BookingResponse updateBooking(long userId, UpdateBookingRequest request);

    void cancelBooking(long userId, long bookingId);

    BookingResponse getBookingById(long userId, long bookingId);

    List<BookingResponse> getAllBookingsByUserId(long userId);
}
