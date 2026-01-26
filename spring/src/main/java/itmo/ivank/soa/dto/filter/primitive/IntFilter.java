package itmo.ivank.soa.dto.filter.primitive;

public record IntFilter(
        Integer eq,
        Integer gt,
        Integer ge,
        Integer lt,
        Integer le
) {
}
