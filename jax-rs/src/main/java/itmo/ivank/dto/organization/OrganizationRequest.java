package itmo.ivank.dto.organization;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationRequest {

    private String name;
    private Coordinates coordinates;
    private Float annualTurnover;
    private String fullName;
    private OrganizationType type;
    private Address officialAddress;

}
