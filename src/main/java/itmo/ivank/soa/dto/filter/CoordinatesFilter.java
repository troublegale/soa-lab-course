package itmo.ivank.soa.dto.filter;

import itmo.ivank.soa.dto.filter.primitive.IntFilter;
import itmo.ivank.soa.dto.filter.primitive.LongFilter;
import jakarta.validation.Valid;

public record CoordinatesFilter(
        @Valid IntFilter xFilter,
        @Valid LongFilter yFilter
) {
}
