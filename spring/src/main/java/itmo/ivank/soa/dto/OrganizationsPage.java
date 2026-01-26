package itmo.ivank.soa.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import itmo.ivank.soa.entity.Organization;

import java.util.List;

@JacksonXmlRootElement(localName = "organizationsPage")
public record OrganizationsPage(
        @JacksonXmlElementWrapper(localName = "organizations")
        @JacksonXmlProperty(localName = "organization")
        List<Organization> organizations,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages
) {
}
