package itmo.ivank.soa.exception;

public class InvalidSearchQueryException extends RuntimeException {
    public InvalidSearchQueryException(String message) {
        super(message);
    }
}
