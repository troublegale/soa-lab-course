package itmo.ivank.dto.organization;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement
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
