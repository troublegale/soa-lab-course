package itmo.ivank.ejb.dto.filter;

import java.io.Serializable;
import java.time.LocalDate;

public record DateFilter(
        LocalDate eq,
        LocalDate before,
        LocalDate after
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
