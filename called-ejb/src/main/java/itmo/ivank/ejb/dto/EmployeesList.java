package itmo.ivank.ejb.dto;

import itmo.ivank.ejb.entity.Employee;

import java.io.Serializable;
import java.util.List;

public record EmployeesList(
        List<Employee> employees
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
