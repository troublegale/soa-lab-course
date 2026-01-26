package itmo.ivank.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@XmlRootElement(name = "ids")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdRequest {

    private List<Long> ids;

    @XmlElement(name = "id")
    public List<Long> getIds() {
        return ids;
    }

}
