package itmo.ivank.soa.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "typeCounts")
public record TypeCountResponse(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "typeCount")
        List<TypeCount> types
) {

}
