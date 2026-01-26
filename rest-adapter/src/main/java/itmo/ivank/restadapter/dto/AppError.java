package itmo.ivank.restadapter.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "AppError")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppError {
    private Integer code;
    private String message;
}
