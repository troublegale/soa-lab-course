package itmo.ivank.restadapter.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "acquiring")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Acquiring {
    private Organization acquirerOrganization;
    private Organization acquiredOrganization;
    private Integer numberOfEmployeesMoved;
}
