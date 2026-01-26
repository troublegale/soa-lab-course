package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "coordinates")
@XmlAccessorType(XmlAccessType.FIELD)
public class Coordinates {
    @XmlElement
    private Long x;
    
    @XmlElement
    private Float y;

    public Coordinates() {}

    public Coordinates(Long x, Float y) {
        this.x = x;
        this.y = y;
    }

    public Long getX() { return x; }
    public void setX(Long x) { this.x = x; }

    public Float getY() { return y; }
    public void setY(Float y) { this.y = y; }
}
