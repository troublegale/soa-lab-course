package itmo.ivank.soa.dto.filter;

import itmo.ivank.soa.dto.filter.primitive.StringFilter;
import jakarta.validation.Valid;

public record AddressFilter(
        @Valid StringFilter streetFilter,
        @Valid LocationFilter townFilter
) {
}
