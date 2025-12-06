package itmo.ivank.dto.employee;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequest  {

    private String name;
    private Long salary;
    private Long organizationId;
}
