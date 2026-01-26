package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Organization")
@XmlAccessorType(XmlAccessType.FIELD)
public class Organization {
    @XmlElement
    private Long id;
    
    @XmlElement
    private String name;
    
    @XmlElement
    private String creationDate;
    
    @XmlElement
    private Float annualTurnover;
    
    @XmlElement
    private String fullName;
    
    @XmlElement
    private Coordinates coordinates;
    
    @XmlElement
    private OrganizationType type;
    
    @XmlElement
    private Address officialAddress;

    public Organization() {}

    public Organization(Long id, String name, String creationDate, Float annualTurnover, 
                        String fullName, Coordinates coordinates, OrganizationType type, 
                        Address officialAddress) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.annualTurnover = annualTurnover;
        this.fullName = fullName;
        this.coordinates = coordinates;
        this.type = type;
        this.officialAddress = officialAddress;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public Float getAnnualTurnover() { return annualTurnover; }
    public void setAnnualTurnover(Float annualTurnover) { this.annualTurnover = annualTurnover; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Coordinates getCoordinates() { return coordinates; }
    public void setCoordinates(Coordinates coordinates) { this.coordinates = coordinates; }

    public OrganizationType getType() { return type; }
    public void setType(OrganizationType type) { this.type = type; }

    public Address getOfficialAddress() { return officialAddress; }
    public void setOfficialAddress(Address officialAddress) { this.officialAddress = officialAddress; }
}
