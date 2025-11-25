package itmo.ivank.soa.dto;

import itmo.ivank.soa.dto.filter.AddressFilter;
import itmo.ivank.soa.dto.filter.CoordinatesFilter;
import itmo.ivank.soa.dto.filter.primitive.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrganizationQuery(
        @Valid @Size(min = 1) List<String> sort,
        @Valid NumberFilter<Long> idFilter,
        @Valid StringFilter nameFilter,
        @Valid CoordinatesFilter coordinatesFilter,
        @Valid DateFilter creationDateFilter,
        @Valid NumberFilter<Float> annualTurnoverFilter,
        @Valid StringFilter fullNameFilter,
        @Valid TypeFilter typeFilter,
        @Valid AddressFilter officialAddressFilter
) {
}
