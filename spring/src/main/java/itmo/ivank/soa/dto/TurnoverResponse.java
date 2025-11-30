package itmo.ivank.soa.dto;

public record TurnoverResponse(
        Float totalTurnover,
        Integer organizationCount
) {
}
