package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "employeeCount")
@XmlAccessorType(XmlAccessType.FIELD)
public class FireResponse {
    @XmlValue
    private Integer employeeCount;

    public FireResponse() {}

    public FireResponse(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }
}
