package itmo.ivank.caller.exception;

import itmo.ivank.caller.dto.AppError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<AppError> handleServiceException(ServiceException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_XML)
                .body(new AppError(500, e.getMessage()));
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<AppError> handleClientException(ClientException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML)
                .body(new AppError(400, e.getMessage()));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<AppError> handleApiException(ApiException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .contentType(MediaType.APPLICATION_XML)
                .body(new AppError(502, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppError> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_XML)
                .body(new AppError(500, "Internal server error: " + e.getMessage()));
    }
}
