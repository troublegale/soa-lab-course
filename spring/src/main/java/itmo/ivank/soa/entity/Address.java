package itmo.ivank.soa.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @NotNull
    @Length(max = 147)
    @Size(max = 147)
    private String street;

    @Embedded
    @Valid
    private Location town;

}
