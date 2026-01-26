package itmo.ivank.soa.dto.filter.primitive;

import java.time.LocalDate;

public record DateFilter(
        LocalDate eq,
        LocalDate before,
        LocalDate after
) {
}
