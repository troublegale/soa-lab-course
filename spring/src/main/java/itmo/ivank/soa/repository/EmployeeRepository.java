package itmo.ivank.soa.repository;

import itmo.ivank.soa.entity.Employee;
import itmo.ivank.soa.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Page<Employee> findByOrganization(Organization organization, Pageable pageable);

    List<Employee> findByOrganization(Organization organization);

}
