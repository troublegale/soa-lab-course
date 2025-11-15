package itmo.ivank.soa.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record Location(
        @NotNull Float x,
        @NotNull Long y,
        @NotNull String name
) {

}
