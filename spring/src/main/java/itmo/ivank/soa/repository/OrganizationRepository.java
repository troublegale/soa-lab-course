package itmo.ivank.soa.repository;

import itmo.ivank.soa.entity.Organization;
import itmo.ivank.soa.entity.OrganizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {

    Integer countByType(OrganizationType type);

    Page<Organization> findAllByFullNameIsLessThan(String value, Pageable pageable);

    @Modifying
    @Query(value = """
            INSERT INTO organizations (
                            id,
                            name,
                            creation_date,
                            annual_turnover,
                            full_name,
                            coordinates_x,
                            coordinates_y,
                            type,
                            official_address_street,
                            official_address_town_x,
                            official_address_town_y,
                            official_address_town_name
                        ) VALUES (
                            :id,
                            :name,
                            :creationDate,
                            :annualTurnover,
                            :fullName,
                            :coordinatesX,
                            :coordinatesY,
                            :type,
                            :addressStreet,
                            :addressTownX,
                            :addressTownY,
                            :addressTownName
                        )
                        RETURNING *
            """, nativeQuery = true)
    Organization insertRaw(@Param("id") Long id,
                              @Param("name") String name,
                              @Param("creationDate") LocalDate creationDate,
                              @Param("annualTurnover") Float annualTurnover,
                              @Param("fullName") String fullName,
                              @Param("coordinatesX") Long coordinatesX,
                              @Param("coordinatesY") Float coordinatesY,
                              @Param("type") String type,
                              @Param("addressStreet") String addressStreet,
                              @Param("addressTownX") Float addressTownX,
                              @Param("addressTownY") Long addressTownY,
                              @Param("addressTownName") String addressTownName
    );

    default Organization saveRaw(Organization o) {
        return insertRaw(
                o.getId(),
                o.getName(),
                o.getCreationDate(),
                o.getAnnualTurnover(),
                o.getFullName(),

                o.getCoordinates().getX(),
                o.getCoordinates().getY(),

                o.getType().name(),

                o.getOfficialAddress() != null ? o.getOfficialAddress().getStreet() : null,
                o.getOfficialAddress() != null && o.getOfficialAddress().getTown() != null ? o.getOfficialAddress().getTown().getX() : null,
                o.getOfficialAddress() != null && o.getOfficialAddress().getTown() != null ? o.getOfficialAddress().getTown().getY() : null,
                o.getOfficialAddress() != null && o.getOfficialAddress().getTown() != null ? o.getOfficialAddress().getTown().getName() : null
        );
    }

}
