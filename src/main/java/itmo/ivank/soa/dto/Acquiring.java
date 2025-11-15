package itmo.ivank.soa.dto;

import itmo.ivank.soa.entity.Organization;

public record Acquiring(
        Organization acquirerOrganization,
        Organization acquiredOrganization,
        Long numberOfEmployeesMoved
) {
}
