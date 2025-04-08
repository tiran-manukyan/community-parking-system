package tir.parkingsystem.exception;

public class AvailableSpotNotFoundException extends RuntimeException {

    public AvailableSpotNotFoundException(long buildingId) {
        super("Available spot in building " + buildingId + " not found");
    }
}
