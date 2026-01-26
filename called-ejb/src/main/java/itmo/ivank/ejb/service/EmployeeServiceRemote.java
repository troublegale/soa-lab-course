package itmo.ivank.ejb.service;

import itmo.ivank.ejb.dto.EmployeeRequest;
import itmo.ivank.ejb.dto.EmployeesList;
import itmo.ivank.ejb.entity.Employee;
import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface EmployeeServiceRemote {

    Employee create(EmployeeRequest dto);

    Employee update(Long id, EmployeeRequest dto);

    Employee getById(Long id);

    void deleteById(Long id);

    EmployeesList createBatch(List<EmployeeRequest> batch);

    void deleteBatch(List<Long> ids);

    EmployeesList updateBatch(List<EmployeeRequest> batch);

}
