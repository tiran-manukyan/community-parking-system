package tir.parkingsystem.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tir.parkingsystem.exception.AvailableSpotNotFoundException;
import tir.parkingsystem.exception.ParkingIllegalStateException;

import java.util.List;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpectedException(Exception e) {
        log.error("Request failed with unexpected exception: {}", e.getMessage(), e);

        ApiError error = new ApiError("Something went wrong", List.of());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> methodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn("Request rejected due to validation failure of method argument: {}", e.getMessage());

        List<ApiError.ApiErrorDetails> details = e.getAllErrors().stream()
                .map(this::mapValidationError)
                .toList();

        ApiError validationFailed = new ApiError("Validation failed", details);

        return new ResponseEntity<>(validationFailed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AvailableSpotNotFoundException.class)
    public ResponseEntity<ApiError> availableSpotNotFoundException(AvailableSpotNotFoundException e) {
        ApiError validationFailed = new ApiError(e.getMessage(), List.of());

        return new ResponseEntity<>(validationFailed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ParkingIllegalStateException.class)
    public ResponseEntity<ApiError> parkingIllegalStateException(ParkingIllegalStateException e) {
        ApiError validationFailed = new ApiError(e.getMessage(), List.of());

        return new ResponseEntity<>(validationFailed, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        ApiError validationFailed = new ApiError(e.getMessage(), List.of());

        return new ResponseEntity<>(validationFailed, HttpStatus.BAD_REQUEST);
    }

    private ApiError.ApiErrorDetails mapValidationError(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return new ApiError.ApiErrorDetails(fieldError.getCode(), fieldError.getField(), error.getDefaultMessage());
        } else if (error instanceof ObjectError objectError) {
            return new ApiError.ApiErrorDetails(objectError.getCode(), objectError.getObjectName(), error.getDefaultMessage());
        } else {
            String[] codes = error.getCodes();
            String code = codes != null && codes.length > 0 ? codes[0] : null;
            return new ApiError.ApiErrorDetails(code, null, error.getDefaultMessage());
        }
    }
}
