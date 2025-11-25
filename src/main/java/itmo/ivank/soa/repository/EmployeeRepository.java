package itmo.ivank.soa.repository;

import itmo.ivank.soa.entity.Employee;
import itmo.ivank.soa.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    @Query(
            "SELECT em FROM Employee em WHERE COALESCE(:name, NULL) IS NULL OR em.name ILIKE CONCAT('%', CAST(:name AS string ), '%') "
    )
    List<Employee> findByOrganization(Organization organization);

}
