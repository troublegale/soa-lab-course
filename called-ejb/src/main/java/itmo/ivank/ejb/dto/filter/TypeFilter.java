package itmo.ivank.ejb.dto.filter;

import itmo.ivank.ejb.entity.OrganizationType;

import java.io.Serializable;
import java.util.List;

public record TypeFilter(
        OrganizationType eq,
        List<OrganizationType> in
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
