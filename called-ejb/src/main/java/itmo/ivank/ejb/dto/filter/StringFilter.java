package itmo.ivank.ejb.dto.filter;

import java.io.Serializable;

public record StringFilter(
        String eq,
        String contains,
        String startsWith,
        String endsWith
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
