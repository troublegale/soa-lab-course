package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "acquiring")
@XmlAccessorType(XmlAccessType.FIELD)
public class Acquiring {
    @XmlElement
    private Organization acquirerOrganization;
    
    @XmlElement
    private Organization acquiredOrganization;
    
    @XmlElement
    private Integer numberOfEmployeesMoved;

    public Acquiring() {}

    public Acquiring(Organization acquirerOrganization, Organization acquiredOrganization, 
                     Integer numberOfEmployeesMoved) {
        this.acquirerOrganization = acquirerOrganization;
        this.acquiredOrganization = acquiredOrganization;
        this.numberOfEmployeesMoved = numberOfEmployeesMoved;
    }

    public Organization getAcquirerOrganization() { return acquirerOrganization; }
    public void setAcquirerOrganization(Organization acquirerOrganization) { 
        this.acquirerOrganization = acquirerOrganization; 
    }

    public Organization getAcquiredOrganization() { return acquiredOrganization; }
    public void setAcquiredOrganization(Organization acquiredOrganization) { 
        this.acquiredOrganization = acquiredOrganization; 
    }

    public Integer getNumberOfEmployeesMoved() { return numberOfEmployeesMoved; }
    public void setNumberOfEmployeesMoved(Integer numberOfEmployeesMoved) { 
        this.numberOfEmployeesMoved = numberOfEmployeesMoved; 
    }
}
