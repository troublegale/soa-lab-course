package itmo.ivank.restadapter.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "location")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private Float x;
    private Long y;
    private String name;
}
