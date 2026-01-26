package itmo.ivank.ejb.dto;

import itmo.ivank.ejb.entity.Address;
import itmo.ivank.ejb.entity.Coordinates;
import itmo.ivank.ejb.entity.OrganizationType;

import java.io.Serializable;
import java.time.LocalDate;

public record OrganizationRequest(
        Long id,
        String name,
        LocalDate creationDate,
        Coordinates coordinates,
        Float annualTurnover,
        String fullName,
        OrganizationType type,
        Address officialAddress
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
