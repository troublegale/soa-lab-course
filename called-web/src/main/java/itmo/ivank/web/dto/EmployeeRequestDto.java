package itmo.ivank.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "employeeRequest")
public class EmployeeRequestDto {

    @Positive
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @Positive
    private Long salary;

    @NotNull
    private Long organizationId;

    public itmo.ivank.ejb.dto.EmployeeRequest toEjbDto() {
        return new itmo.ivank.ejb.dto.EmployeeRequest(id, name, salary, organizationId);
    }
}
