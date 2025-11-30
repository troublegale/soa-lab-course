package itmo.ivank.soa.dto;

import itmo.ivank.soa.entity.OrganizationType;

public record TypeCount(
        OrganizationType type,
        Integer count
) {
}
