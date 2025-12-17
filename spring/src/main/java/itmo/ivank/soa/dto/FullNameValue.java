package itmo.ivank.soa.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "fullNameValue")
public record FullNameValue(
        String value
) {
}
