package itmo.ivank.soa.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import itmo.ivank.soa.entity.Employee;

import java.util.List;

@JacksonXmlRootElement(localName = "employees")
public record EmployeesList(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "employee")
        List<Employee> employees
) {
}
