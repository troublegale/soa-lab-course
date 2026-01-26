package itmo.ivank.soap.exception;

import javax.xml.ws.WebFault;

@WebFault(name = "CallerServiceFault", targetNamespace = "http://soap.ivank.itmo/caller")
public class CallerServiceException extends Exception {
    
    private final CallerServiceFault faultInfo;

    public CallerServiceException(String message, CallerServiceFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public CallerServiceException(String message, CallerServiceFault faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public CallerServiceFault getFaultInfo() {
        return faultInfo;
    }
}
