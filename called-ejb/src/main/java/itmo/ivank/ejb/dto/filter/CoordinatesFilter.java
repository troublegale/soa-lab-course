package itmo.ivank.ejb.dto.filter;

import java.io.Serializable;

public record CoordinatesFilter(
        NumberFilter<Long> xFilter,
        NumberFilter<Float> yFilter
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
