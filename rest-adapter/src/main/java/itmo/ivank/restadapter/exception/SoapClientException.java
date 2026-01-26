package itmo.ivank.restadapter.exception;

public class SoapClientException extends RuntimeException {
    private final int code;
    
    public SoapClientException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
