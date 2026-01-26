package itmo.ivank.ejb.dto;

import java.io.Serializable;

public record TurnoverResponse(
        Double totalTurnover,
        Integer organizationCount
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
