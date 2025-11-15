package itmo.ivank.soa.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record Coordinates(
        @NotNull @Min(-826) Long x,
        @NotNull @Min(-143) Float y
) {

}
