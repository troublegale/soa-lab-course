package itmo.ivank.ejb.service;

import itmo.ivank.ejb.dto.EmployeeRequest;
import itmo.ivank.ejb.dto.EmployeesList;
import itmo.ivank.ejb.entity.Employee;
import itmo.ivank.ejb.entity.Organization;
import itmo.ivank.ejb.exception.NotFoundException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class EmployeeServiceBean implements EmployeeServiceRemote {

    @PersistenceContext(unitName = "soaPU")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Employee create(EmployeeRequest dto) {
        Organization org = em.find(Organization.class, dto.organizationId());
        if (org == null) {
            throw new NotFoundException("Organization not found: " + dto.organizationId());
        }
        Employee employee = Employee.builder()
                .name(dto.name())
                .salary(dto.salary())
                .organization(org)
                .build();
        em.persist(employee);
        return employee;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Employee update(Long id, EmployeeRequest dto) {
        Employee employee = em.find(Employee.class, id);
        if (employee == null) {
            throw new NotFoundException("Employee not found: " + id);
        }
        Organization org = em.find(Organization.class, dto.organizationId());
        if (org == null) {
            throw new NotFoundException("Organization not found: " + dto.organizationId());
        }
        employee.setName(dto.name());
        employee.setSalary(dto.salary());
        employee.setOrganization(org);
        return em.merge(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = em.find(Employee.class, id);
        if (employee == null) {
            throw new NotFoundException("Employee not found: " + id);
        }
        return employee;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteById(Long id) {
        Employee employee = em.find(Employee.class, id);
        if (employee == null) {
            throw new NotFoundException("Employee not found: " + id);
        }
        em.remove(employee);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public EmployeesList createBatch(List<EmployeeRequest> batch) {
        List<Employee> employees = new ArrayList<>();
        for (EmployeeRequest dto : batch) {
            employees.add(create(dto));
        }
        return new EmployeesList(employees);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Employee employee = em.find(Employee.class, id);
            if (employee != null) {
                em.remove(employee);
            }
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public EmployeesList updateBatch(List<EmployeeRequest> batch) {
        List<Employee> employees = new ArrayList<>();
        for (EmployeeRequest emp : batch) {
            if (emp.id() == null) {
                throw new NotFoundException("Employee id is required for update");
            }
            employees.add(update(emp.id(), emp));
        }
        return new EmployeesList(employees);
    }

}
