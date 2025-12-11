package itmo.ivank.soa.service;

import itmo.ivank.soa.dto.*;
import itmo.ivank.soa.entity.Organization;
import itmo.ivank.soa.entity.OrganizationType;
import itmo.ivank.soa.exception.InvalidSearchQueryException;
import itmo.ivank.soa.repository.EmployeeRepository;
import itmo.ivank.soa.repository.OrganizationRepository;
import itmo.ivank.soa.util.SpecificationBuilder;
import itmo.ivank.soa.util.SortBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;

    public Organization getById(Long id) {
        return organizationRepository.findById(id).orElseThrow();
    }

    public OrganizationsPage getAll(Integer page, Integer size) {
        Page<Organization> p = organizationRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id")));
        return new OrganizationsPage(
                p.getContent(),
                p.getNumber() + 1,
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages()
        );
    }

    public Organization create(OrganizationRequest dto) {
        var organization = Organization.builder()
                .name(dto.name())
                .coordinates(dto.coordinates())
                .annualTurnover(dto.annualTurnover())
                .fullName(dto.fullName())
                .type(dto.type())
                .officialAddress(dto.officialAddress())
                .build();
        return organizationRepository.save(organization);
    }

    public Organization createRaw(OrganizationRequest dto) {
        var organization = Organization.builder()
                .id(dto.id())
                .name(dto.name())
                .creationDate(dto.creationDate())
                .coordinates(dto.coordinates())
                .annualTurnover(dto.annualTurnover())
                .fullName(dto.fullName())
                .type(dto.type())
                .officialAddress(dto.officialAddress())
                .build();
        return organizationRepository.saveRaw(organization);
    }

    public Organization update(Long id, OrganizationRequest dto) {
        var organization = getById(id);
        organization.setName(dto.name());
        organization.setCoordinates(dto.coordinates());
        organization.setAnnualTurnover(dto.annualTurnover());
        organization.setFullName(dto.fullName());
        organization.setType(dto.type());
        organization.setOfficialAddress(dto.officialAddress());
        return organizationRepository.save(organization);
    }

    public void delete(Long id) {
        var organization = getById(id);
        organizationRepository.delete(organization);
    }

    public EmployeesList getEmployees(Long id) {
        var organization = getById(id);
        return new EmployeesList(employeeRepository.findByOrganization(organization));
    }

    public TurnoverResponse getTotalTurnover() {
        List<Organization> organizations = organizationRepository.findAll();
        Double total = organizations.stream().map(org ->
                Double.valueOf(org.getAnnualTurnover())).reduce(0.0, Double::sum);
        return new TurnoverResponse(total, organizations.size());
    }

    public List<TypeCount> getOrganizationTypesCount() {
        List<OrganizationType> types = List.of(OrganizationType.values());
        List<TypeCount> typeCounts = new ArrayList<>();
        types.forEach(type -> typeCounts.add(
                new TypeCount(type, organizationRepository.countByType(type))));
        return typeCounts;
    }

    public OrganizationsPage getFiltered(Integer page, Integer size, OrganizationQuery query) {
        try {
            Sort sort = SortBuilder.buildSort(query.sort(), "id");
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Specification<Organization> spec = SpecificationBuilder.buildSpecification(query);
            Page<Organization> p = organizationRepository.findAll(spec, pageable);
            return new OrganizationsPage(
                    p.getContent(),
                    p.getNumber() + 1,
                    p.getSize(),
                    p.getTotalElements(),
                    p.getTotalPages()
            );
        } catch (Exception e) {
            throw new InvalidSearchQueryException("Invalid filters or sorting parameters");
        }
    }

    public OrganizationsPage getOrganizationsLessThanFullName(String value, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Organization> p = organizationRepository.findAllByFullNameIsLessThan(value, pageable);
        return new OrganizationsPage(
                p.getContent(),
                p.getNumber() + 1,
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages()
        );
    }

}
