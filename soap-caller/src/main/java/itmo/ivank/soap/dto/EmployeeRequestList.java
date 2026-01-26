package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "employees")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmployeeRequestList {
    @XmlElement(name = "employee")
    private List<EmployeeRequest> employees;

    public EmployeeRequestList() {}

    public EmployeeRequestList(List<EmployeeRequest> employees) {
        this.employees = employees;
    }

    public List<EmployeeRequest> getEmployees() { return employees; }
    public void setEmployees(List<EmployeeRequest> employees) { this.employees = employees; }
}
