package itmo.ivank.restadapter.exception;

import itmo.ivank.restadapter.dto.AppError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SoapClientException.class)
    public ResponseEntity<AppError> handleSoapClientException(SoapClientException e) {
        int code = e.getCode();
        HttpStatus status = HttpStatus.resolve(code);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_XML)
                .body(new AppError(code, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppError> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_XML)
                .body(new AppError(500, "Internal server error: " + e.getMessage()));
    }
}
