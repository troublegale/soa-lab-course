package itmo.ivank.soa.service;

import itmo.ivank.soa.dto.EmployeeRequest;
import itmo.ivank.soa.entity.Employee;
import itmo.ivank.soa.repository.EmployeeRepository;
import itmo.ivank.soa.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;

    public Employee createEmployee(EmployeeRequest dto) {
        var org = organizationRepository.findById(dto.organizationId()).orElseThrow();
        var employee = Employee.builder()
                .name(dto.name())
                .salary(dto.salary())
                .organization(org)
                .build();
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, EmployeeRequest dto) {
        var employee =  employeeRepository.findById(id).orElseThrow();
        employee.setName(dto.name());
        employee.setSalary(dto.salary());
        employee.setOrganization(organizationRepository.findById(dto.organizationId()).orElseThrow());
        return employeeRepository.save(employee);
    }

    public Employee getById(Long id) {
        return employeeRepository.findById(id).orElseThrow();
    }

    public void deleteById(Long id) {
        var employee = employeeRepository.findById(id).orElseThrow();
        employeeRepository.delete(employee);
    }

}
