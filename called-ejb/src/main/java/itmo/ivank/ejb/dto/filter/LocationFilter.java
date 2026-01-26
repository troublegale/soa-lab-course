package itmo.ivank.ejb.dto.filter;

import java.io.Serializable;

public record LocationFilter(
        NumberFilter<Float> xFilter,
        NumberFilter<Long> yFilter,
        StringFilter nameFilter
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
