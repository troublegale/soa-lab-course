package itmo.ivank.soa.dto.filter.primitive;

public record LongFilter(
        Long eq,
        Long gt,
        Long ge,
        Long lt,
        Long le
) {
}
