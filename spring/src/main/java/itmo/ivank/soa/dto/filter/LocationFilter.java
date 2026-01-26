package itmo.ivank.soa.dto.filter;

import itmo.ivank.soa.dto.filter.primitive.NumberFilter;
import itmo.ivank.soa.dto.filter.primitive.StringFilter;
import jakarta.validation.Valid;

public record LocationFilter(
        @Valid NumberFilter<Float> xFilter,
        @Valid NumberFilter<Long> yFilter,
        @Valid StringFilter nameFilter
) {
}
