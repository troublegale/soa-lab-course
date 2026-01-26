package itmo.ivank.ejb.dto.filter;

import java.io.Serializable;

public record AddressFilter(
        StringFilter streetFilter,
        LocationFilter townFilter
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
