package itmo.ivank.caller.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@XmlRootElement(name = "employees")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequestList {
    private List<EmployeeRequest> employees;

    @XmlElement(name = "employee")
    public List<EmployeeRequest> getEmployeeRequests() {
        return employees;
    }
}
