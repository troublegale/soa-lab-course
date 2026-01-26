package itmo.ivank.ejb.dto;

import itmo.ivank.ejb.entity.Organization;

import java.io.Serializable;
import java.util.List;

public record OrganizationsPage(
        List<Organization> organizations,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
