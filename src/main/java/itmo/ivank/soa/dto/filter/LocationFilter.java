package itmo.ivank.soa.dto.filter;

import itmo.ivank.soa.dto.filter.primitive.FloatFilter;
import itmo.ivank.soa.dto.filter.primitive.IntFilter;
import itmo.ivank.soa.dto.filter.primitive.StringFilter;

public record LocationFilter(
        FloatFilter xFilter,
        IntFilter yFilter,
        StringFilter nameFilter
) {
}
