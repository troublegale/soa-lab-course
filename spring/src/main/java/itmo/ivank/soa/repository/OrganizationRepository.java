package itmo.ivank.soa.repository;

import itmo.ivank.soa.entity.Organization;
import itmo.ivank.soa.entity.OrganizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {

    Integer countByType(OrganizationType type);

    Page<Organization> findAllByFullNameIsLessThan(String value, Pageable pageable);

}
