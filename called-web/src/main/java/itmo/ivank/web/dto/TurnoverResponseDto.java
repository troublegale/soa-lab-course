package itmo.ivank.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "turnoverResponse")
public class TurnoverResponseDto {
    private Double totalTurnover;
    private Integer organizationCount;

    public static TurnoverResponseDto from(itmo.ivank.ejb.dto.TurnoverResponse resp) {
        return new TurnoverResponseDto(resp.totalTurnover(), resp.organizationCount());
    }
}
