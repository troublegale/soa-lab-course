package itmo.ivank.soa.service;

import itmo.ivank.soa.dto.EmployeeRequest;
import itmo.ivank.soa.dto.EmployeesList;
import itmo.ivank.soa.entity.Employee;
import itmo.ivank.soa.repository.EmployeeRepository;
import itmo.ivank.soa.repository.OrganizationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Employee create(EmployeeRequest dto) {
        var org = organizationRepository.findById(dto.organizationId()).orElseThrow();
        var employee = Employee.builder()
                .name(dto.name())
                .salary(dto.salary())
                .organization(org)
                .build();
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Long id, EmployeeRequest dto) {
        var employee =  employeeRepository.findById(id).orElseThrow();
        employee.setName(dto.name());
        employee.setSalary(dto.salary());
        employee.setOrganization(organizationRepository.findById(dto.organizationId()).orElseThrow());
        return employeeRepository.save(employee);
    }

    public Employee getById(Long id) {
        return employeeRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void deleteById(Long id) {
        var employee = employeeRepository.findById(id).orElseThrow();
        employeeRepository.delete(employee);
    }

    @Transactional
    public EmployeesList createBatch(List<EmployeeRequest> batch) {
        List<Employee> employees = new ArrayList<>();
        for (var dto : batch) {
            employees.add(create(dto));
        }
        return new EmployeesList(employees);
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        employeeRepository.deleteAllById(ids);
    }

    @Transactional
    public EmployeesList updateBatch(List<EmployeeRequest> batch) {
        List<Employee> employees = new ArrayList<>();
        for (var emp : batch) {
            if (emp.id() == null || !employeeRepository.existsById(emp.id())) throw new NoSuchElementException();
            employees.add(update(emp.id(), emp));
        }
        return new EmployeesList(employees);
    }

}
