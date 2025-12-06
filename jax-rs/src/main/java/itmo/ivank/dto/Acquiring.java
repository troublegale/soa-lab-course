package itmo.ivank.dto;

import itmo.ivank.dto.organization.Organization;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Acquiring {
    private Organization acquirerOrganization;
    private Organization acquiredOrganization;
    private Integer numberOfEmployeesMoved;
}
