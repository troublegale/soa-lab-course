package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "address")
@XmlAccessorType(XmlAccessType.FIELD)
public class Address {
    @XmlElement
    private String street;
    
    @XmlElement
    private Location town;

    public Address() {}

    public Address(String street, Location town) {
        this.street = street;
        this.town = town;
    }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public Location getTown() { return town; }
    public void setTown(Location town) { this.town = town; }
}
