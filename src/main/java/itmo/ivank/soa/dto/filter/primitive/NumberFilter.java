package itmo.ivank.soa.dto.filter.primitive;

public record NumberFilter<T extends Number>(
        T eq,
        T gt,
        T ge,
        T lt,
        T le
) {
}
