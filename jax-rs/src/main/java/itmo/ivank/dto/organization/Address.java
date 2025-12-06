package itmo.ivank.dto.organization;

import lombok.Data;

@Data
public class Address {
    private String street;
    private Location town;
}
