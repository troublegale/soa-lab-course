package itmo.ivank.ejb.dto;

import itmo.ivank.ejb.entity.OrganizationType;

import java.io.Serializable;

public record TypeCount(
        OrganizationType type,
        Integer count
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
