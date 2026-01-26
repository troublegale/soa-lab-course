package itmo.ivank.ejb.dto.filter;

import java.io.Serializable;

public record NumberFilter<T extends Number & Comparable<T>>(
        T eq,
        T gt,
        T ge,
        T lt,
        T le
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
