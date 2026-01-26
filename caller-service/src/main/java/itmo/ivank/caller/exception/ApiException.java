package itmo.ivank.caller.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
