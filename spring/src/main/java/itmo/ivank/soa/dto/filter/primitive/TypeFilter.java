package itmo.ivank.soa.dto.filter.primitive;

import itmo.ivank.soa.entity.OrganizationType;

import java.util.List;

public record TypeFilter(
        OrganizationType eq,
        List<OrganizationType> in
){
}
