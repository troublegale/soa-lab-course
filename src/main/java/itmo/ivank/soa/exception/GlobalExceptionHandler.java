package itmo.ivank.soa.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import itmo.ivank.soa.dto.AppError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        List<String> messages = ex.getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .toList();
        String message = messages.stream().reduce("", (a, b) -> a + "\n" + b);

        AppError error = new AppError(400, message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<AppError> handleUnrecognizedPropertyException(UnrecognizedPropertyException ex) {
        String message = String.format("Unknown field: %s", ex.getPropertyName());
        AppError error = new AppError(400, message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AppError> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = ex.getMessage();
        AppError error = new AppError(400, message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<AppError> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        String message = ex.getMessage();
        AppError error = new AppError(405, message);
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<AppError> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        String message = "Endpoint not found";
        AppError error = new AppError(404, message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<AppError> handleNoSuchElementException(NoSuchElementException ex) {
        String message = ex.getMessage();
        AppError error = new AppError(404, message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

}
