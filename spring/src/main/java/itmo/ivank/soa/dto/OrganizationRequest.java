package itmo.ivank.soa.dto;

import itmo.ivank.soa.entity.Address;
import itmo.ivank.soa.entity.Coordinates;
import itmo.ivank.soa.entity.OrganizationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record OrganizationRequest(
        @Positive Long id,
        @NotNull @NotBlank @Size(max = 255) String name,
        LocalDate creationDate,
        @Valid @NotNull Coordinates coordinates,
        @NotNull @Positive Float annualTurnover,
        @Pattern(regexp = "^(?!\\s*$).+") @Size(max = 255) String fullName,
        @NotNull OrganizationType type,
        @Valid Address officialAddress
) {
}
