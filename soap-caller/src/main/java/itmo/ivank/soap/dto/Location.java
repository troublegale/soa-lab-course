package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "location")
@XmlAccessorType(XmlAccessType.FIELD)
public class Location {
    @XmlElement
    private Float x;
    
    @XmlElement
    private Long y;
    
    @XmlElement
    private String name;

    public Location() {}

    public Location(Float x, Long y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public Float getX() { return x; }
    public void setX(Float x) { this.x = x; }

    public Long getY() { return y; }
    public void setY(Long y) { this.y = y; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
