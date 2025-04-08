package tir.parkingsystem.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long bookingId) {
        super("Booking with ID " + bookingId + " not found");
    }
}
