package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "employee")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmployeeRequest {
    @XmlElement
    private Long id;
    
    @XmlElement
    private String name;
    
    @XmlElement
    private Long salary;
    
    @XmlElement
    private Long organizationId;

    public EmployeeRequest() {}

    public EmployeeRequest(Long id, String name, Long salary, Long organizationId) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.organizationId = organizationId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getSalary() { return salary; }
    public void setSalary(Long salary) { this.salary = salary; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
}
