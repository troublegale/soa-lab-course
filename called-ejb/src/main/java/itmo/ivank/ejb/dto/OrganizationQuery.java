package itmo.ivank.ejb.dto;

import itmo.ivank.ejb.dto.filter.*;

import java.io.Serializable;
import java.util.List;

public record OrganizationQuery(
        List<String> sort,
        NumberFilter<Long> idFilter,
        StringFilter nameFilter,
        CoordinatesFilter coordinatesFilter,
        DateFilter creationDateFilter,
        NumberFilter<Float> annualTurnoverFilter,
        StringFilter fullNameFilter,
        TypeFilter typeFilter,
        AddressFilter officialAddressFilter
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
