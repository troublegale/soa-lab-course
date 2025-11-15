package itmo.ivank.soa.dto.filter.primitive;

public record FloatFilter(
        Float eq,
        Float gt,
        Float ge,
        Float lt,
        Float le
) {
}
