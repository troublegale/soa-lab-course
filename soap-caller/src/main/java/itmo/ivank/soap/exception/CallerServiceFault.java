package itmo.ivank.soap.exception;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "callerServiceFault")
@XmlAccessorType(XmlAccessType.FIELD)
public class CallerServiceFault {
    @XmlElement
    private Integer code;
    
    @XmlElement
    private String message;

    public CallerServiceFault() {}

    public CallerServiceFault(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
