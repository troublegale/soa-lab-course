package itmo.ivank.soa.dto.filter.primitive;

public record StringFilter(
        String eq,
        String contains,
        String startsWith,
        String endsWith
) {
}
