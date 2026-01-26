package itmo.ivank.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import itmo.ivank.ejb.dto.TypeCount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "typeCounts")
public class TypeCountResponseDto {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "typeCount")
    private List<TypeCount> types;

    public static TypeCountResponseDto from(itmo.ivank.ejb.dto.TypeCountResponse resp) {
        return new TypeCountResponseDto(resp.types());
    }
}
