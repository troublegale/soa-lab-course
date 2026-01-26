package itmo.ivank.caller.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "organization")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Organization {
    private Long id;
    private String name;
    private String creationDate;
    private Float annualTurnover;
    private String fullName;
    private Coordinates coordinates;
    private OrganizationType type;
    private Address officialAddress;
}
