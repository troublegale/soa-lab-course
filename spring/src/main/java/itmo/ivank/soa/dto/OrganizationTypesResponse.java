package itmo.ivank.soa.dto;

public record OrganizationTypesResponse(
        Integer commercialCount,
        Integer governmentCount,
        Integer privateLimitedCompanyCount,
        Integer openJointStockCompany
) {
}
