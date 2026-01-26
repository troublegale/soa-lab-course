package itmo.ivank.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import itmo.ivank.ejb.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "organizationsPage")
public class OrganizationsPageDto {
    @JacksonXmlElementWrapper(localName = "organizations")
    @JacksonXmlProperty(localName = "organization")
    private List<Organization> organizations;
    
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

    public static OrganizationsPageDto from(itmo.ivank.ejb.dto.OrganizationsPage page) {
        return new OrganizationsPageDto(
                page.organizations(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }
}
