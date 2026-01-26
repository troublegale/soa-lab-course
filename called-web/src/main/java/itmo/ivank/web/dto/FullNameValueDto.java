package itmo.ivank.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "fullNameValue")
public class FullNameValueDto {
    private String value;
}
