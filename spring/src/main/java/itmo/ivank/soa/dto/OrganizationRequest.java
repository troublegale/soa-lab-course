package itmo.ivank.soa.dto;

import itmo.ivank.soa.entity.Address;
import itmo.ivank.soa.entity.Coordinates;
import itmo.ivank.soa.entity.OrganizationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record OrganizationRequest(
        @NotNull @NotBlank String name,
        @Valid @NotNull Coordinates coordinates,
        @NotNull @Positive Float annualTurnover,
        @Pattern(regexp = "^(?!\\s*$).+") String fullName,
        @NotNull OrganizationType type,
        @Valid Address officialAddress
) {
}
