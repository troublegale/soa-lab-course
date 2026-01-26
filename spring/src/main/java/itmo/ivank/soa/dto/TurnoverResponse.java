package itmo.ivank.soa.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "turnoverResponse")
public record TurnoverResponse(
        Double totalTurnover,
        Integer organizationCount
) {
}
