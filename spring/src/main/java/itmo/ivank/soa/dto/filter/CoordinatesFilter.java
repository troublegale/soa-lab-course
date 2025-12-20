package itmo.ivank.soa.dto.filter;

import itmo.ivank.soa.dto.filter.primitive.NumberFilter;
import jakarta.validation.Valid;

public record CoordinatesFilter(
        @Valid NumberFilter<Long> xFilter,
        @Valid NumberFilter<Float> yFilter
) {
}
