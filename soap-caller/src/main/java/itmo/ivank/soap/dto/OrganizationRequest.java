package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "organizationRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrganizationRequest {
    @XmlElement
    private String name;
    
    @XmlElement
    private Coordinates coordinates;
    
    @XmlElement
    private Float annualTurnover;
    
    @XmlElement
    private String fullName;
    
    @XmlElement
    private OrganizationType type;
    
    @XmlElement
    private Address officialAddress;

    public OrganizationRequest() {}

    public OrganizationRequest(String name, Coordinates coordinates, Float annualTurnover, 
                               String fullName, OrganizationType type, Address officialAddress) {
        this.name = name;
        this.coordinates = coordinates;
        this.annualTurnover = annualTurnover;
        this.fullName = fullName;
        this.type = type;
        this.officialAddress = officialAddress;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Coordinates getCoordinates() { return coordinates; }
    public void setCoordinates(Coordinates coordinates) { this.coordinates = coordinates; }

    public Float getAnnualTurnover() { return annualTurnover; }
    public void setAnnualTurnover(Float annualTurnover) { this.annualTurnover = annualTurnover; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public OrganizationType getType() { return type; }
    public void setType(OrganizationType type) { this.type = type; }

    public Address getOfficialAddress() { return officialAddress; }
    public void setOfficialAddress(Address officialAddress) { this.officialAddress = officialAddress; }
}
