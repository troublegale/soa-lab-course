package itmo.ivank.caller.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "employeeCount")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FireResponse {
    @JacksonXmlText
    private Integer employeeCount;
}
