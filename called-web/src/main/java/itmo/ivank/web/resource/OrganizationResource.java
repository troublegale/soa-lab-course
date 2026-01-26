package itmo.ivank.web.resource;

import itmo.ivank.ejb.entity.Organization;
import itmo.ivank.ejb.service.OrganizationServiceRemote;
import itmo.ivank.web.dto.*;
import itmo.ivank.web.util.EjbLookup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.logging.Logger;

@Path("/organizations")
public class OrganizationResource {

    private static final Logger logger = Logger.getLogger(OrganizationResource.class.getName());

    private OrganizationServiceRemote getService() {
        return EjbLookup.getOrganizationService();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public OrganizationsPageDto getAllOrganizations(
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("20") Integer size) {
        logger.info("getAllOrganizations called");
        return OrganizationsPageDto.from(getService().getAll(page, size));
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Organization createOrganization(@Valid @NotNull OrganizationRequestDto request) {
        return getService().create(request.toEjbDto());
    }

    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public OrganizationsPageDto getFilteredOrganizations(
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("20") Integer size,
            @Valid @NotNull OrganizationQueryDto query) {
        return OrganizationsPageDto.from(getService().getFiltered(page, size, query.toEjbDto()));
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Organization getOrganization(@PathParam("id") Long id) {
        return getService().getById(id);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Organization updateOrganization(
            @PathParam("id") Long id,
            @Valid @NotNull OrganizationRequestDto request) {
        return getService().update(id, request.toEjbDto());
    }

    @DELETE
    @Path("/{id}")
    public void deleteOrganization(@PathParam("id") Long id) {
        getService().delete(id);
    }

    @GET
    @Path("/{id}/employees")
    @Produces(MediaType.APPLICATION_XML)
    public EmployeesListDto getOrganizationEmployees(@PathParam("id") Long id) {
        return EmployeesListDto.from(getService().getEmployees(id));
    }

    @GET
    @Path("/turnover")
    @Produces(MediaType.APPLICATION_XML)
    public TurnoverResponseDto getTotalTurnover() {
        return TurnoverResponseDto.from(getService().getTotalTurnover());
    }

    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_XML)
    public TypeCountResponseDto getOrganizationTypesCount() {
        return TypeCountResponseDto.from(getService().getOrganizationTypesCount());
    }

    @POST
    @Path("/lt-full-name")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public OrganizationsPageDto getOrganizationsLessThanFullName(
            @NotNull FullNameValueDto fullNameValue,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("20") Integer size) {
        return OrganizationsPageDto.from(
                getService().getOrganizationsLessThanFullName(fullNameValue.getValue(), page, size));
    }

    @POST
    @Path("/compensate")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Organization compensateOrganization(@Valid @NotNull OrganizationRequestDto request) {
        return getService().createRaw(request.toEjbDto());
    }
}
