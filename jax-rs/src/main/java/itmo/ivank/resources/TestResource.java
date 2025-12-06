package itmo.ivank.resources;

import itmo.ivank.dto.employee.EmployeesResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/test")
public class TestResource {

    private static final String BASE_URL = "https://spring-wildfly:8443/soa/api/v1/organizations/1/employees";

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public EmployeesResponse getEmployees() {
        try (Client client = ClientBuilder.newClient()) {
            Response resp = client.target(BASE_URL)
                    .request()
                    .header("Connection", "close")
                    .get();
            return resp.readEntity(EmployeesResponse.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
