package itmo.ivank.soa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

public record EmployeeRequest(
        @NotNull @NotBlank @Length(max = 128) String name,
        @NotNull @Positive Long salary,
        @NotNull Long organizationId
) {
}
