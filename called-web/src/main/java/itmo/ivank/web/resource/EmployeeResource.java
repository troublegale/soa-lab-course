package itmo.ivank.web.resource;

import itmo.ivank.ejb.dto.EmployeeRequest;
import itmo.ivank.ejb.entity.Employee;
import itmo.ivank.ejb.service.EmployeeServiceRemote;
import itmo.ivank.web.dto.EmployeeRequestDto;
import itmo.ivank.web.dto.EmployeesListDto;
import itmo.ivank.web.util.EjbLookup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.stream.Collectors;

@Path("/employees")
public class EmployeeResource {

    private EmployeeServiceRemote getService() {
        return EjbLookup.getEmployeeService();
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Employee createEmployee(@Valid @NotNull EmployeeRequestDto request) {
        return getService().create(request.toEjbDto());
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Employee updateEmployee(
            @PathParam("id") Long id,
            @Valid @NotNull EmployeeRequestDto request) {
        return getService().update(id, request.toEjbDto());
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Employee getEmployee(@PathParam("id") Long id) {
        return getService().getById(id);
    }

    @DELETE
    @Path("/{id}")
    public void deleteEmployee(@PathParam("id") Long id) {
        getService().deleteById(id);
    }

    @POST
    @Path("/batch/create")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public EmployeesListDto createBatch(@Valid @NotNull List<EmployeeRequestDto> employees) {
        List<EmployeeRequest> batch = employees.stream()
                .map(EmployeeRequestDto::toEjbDto)
                .collect(Collectors.toList());
        return EmployeesListDto.from(getService().createBatch(batch));
    }

    @POST
    @Path("/batch/update")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public EmployeesListDto updateBatch(@Valid @NotNull List<EmployeeRequestDto> employees) {
        List<EmployeeRequest> batch = employees.stream()
                .map(EmployeeRequestDto::toEjbDto)
                .collect(Collectors.toList());
        return EmployeesListDto.from(getService().updateBatch(batch));
    }

    @POST
    @Path("/batch/delete")
    @Consumes(MediaType.APPLICATION_XML)
    public void deleteBatch(@Valid @NotNull List<Long> ids) {
        getService().deleteBatch(ids);
    }
}
