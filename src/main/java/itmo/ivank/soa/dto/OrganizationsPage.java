package itmo.ivank.soa.dto;

import itmo.ivank.soa.entity.Organization;

import java.util.List;

public record OrganizationsPage(
        List<Organization> organizations,
        Long page,
        Long size,
        Long totalElements,
        Long totalPages
) {
}
