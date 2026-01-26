package itmo.ivank.ejb.dto;

import java.io.Serializable;

public record EmployeeRequest(
        Long id,
        String name,
        Long salary,
        Long organizationId
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
