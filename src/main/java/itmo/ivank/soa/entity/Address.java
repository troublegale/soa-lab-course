package itmo.ivank.soa.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

@Embeddable
public record Address(
        @NotNull @Length(max = 147) String street,
        @Valid Location town
) {

}
