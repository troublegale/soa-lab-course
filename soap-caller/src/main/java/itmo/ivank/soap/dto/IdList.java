package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ids")
@XmlAccessorType(XmlAccessType.FIELD)
public class IdList {
    @XmlElement(name = "id")
    private List<Long> ids;

    public IdList() {}

    public IdList(List<Long> ids) {
        this.ids = ids;
    }

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
}
