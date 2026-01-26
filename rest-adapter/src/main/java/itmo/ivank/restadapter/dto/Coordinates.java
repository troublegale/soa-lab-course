package itmo.ivank.restadapter.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "coordinates")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coordinates {
    private Long x;
    private Float y;
}
