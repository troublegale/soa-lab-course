package itmo.ivank.restadapter.dto;

import lombok.Data;

@Data
public class Address {
    private String street;
    private Location town;
}
