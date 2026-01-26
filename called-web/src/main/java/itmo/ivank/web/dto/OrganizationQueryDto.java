package itmo.ivank.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import itmo.ivank.ejb.dto.filter.*;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "organizationQuery")
public class OrganizationQueryDto {

    private List<String> sort;
    private NumberFilter<Long> idFilter;
    private StringFilter nameFilter;
    private CoordinatesFilter coordinatesFilter;
    private DateFilter creationDateFilter;
    private NumberFilter<Float> annualTurnoverFilter;
    private StringFilter fullNameFilter;
    private TypeFilter typeFilter;
    private AddressFilter officialAddressFilter;

    public itmo.ivank.ejb.dto.OrganizationQuery toEjbDto() {
        return new itmo.ivank.ejb.dto.OrganizationQuery(
                sort, idFilter, nameFilter, coordinatesFilter,
                creationDateFilter, annualTurnoverFilter, fullNameFilter,
                typeFilter, officialAddressFilter
        );
    }
}
