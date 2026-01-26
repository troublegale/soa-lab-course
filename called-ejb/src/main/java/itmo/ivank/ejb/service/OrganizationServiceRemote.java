package itmo.ivank.ejb.service;

import itmo.ivank.ejb.dto.*;
import itmo.ivank.ejb.entity.Organization;
import jakarta.ejb.Remote;

@Remote
public interface OrganizationServiceRemote {

    Organization getById(Long id);

    OrganizationsPage getAll(Integer page, Integer size);

    Organization create(OrganizationRequest dto);

    Organization createRaw(OrganizationRequest dto);

    Organization update(Long id, OrganizationRequest dto);

    void delete(Long id);

    EmployeesList getEmployees(Long id);

    TurnoverResponse getTotalTurnover();

    TypeCountResponse getOrganizationTypesCount();

    OrganizationsPage getFiltered(Integer page, Integer size, OrganizationQuery query);

    OrganizationsPage getOrganizationsLessThanFullName(String value, Integer page, Integer size);

}
