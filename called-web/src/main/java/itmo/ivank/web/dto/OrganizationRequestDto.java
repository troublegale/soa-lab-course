package itmo.ivank.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import itmo.ivank.ejb.entity.Address;
import itmo.ivank.ejb.entity.Coordinates;
import itmo.ivank.ejb.entity.OrganizationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@JacksonXmlRootElement(localName = "organizationRequest")
public class OrganizationRequestDto {

    @Positive
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 255)
    private String name;

    private LocalDate creationDate;

    @Valid
    @NotNull
    private Coordinates coordinates;

    @NotNull
    @Positive
    private Float annualTurnover;

    @Pattern(regexp = "^$|^(?!\\s*$).+")
    @Size(max = 255)
    private String fullName;

    @NotNull
    private OrganizationType type;

    @Valid
    private Address officialAddress;

    public itmo.ivank.ejb.dto.OrganizationRequest toEjbDto() {
        return new itmo.ivank.ejb.dto.OrganizationRequest(
                id, name, creationDate, coordinates, annualTurnover, fullName, type, officialAddress
        );
    }
}
