package itmo.ivank;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/orgs")
public class OrgsResource {

    private static final String SPRING_URL = "https://spring-wildfly:8443/soa/api/v1/organizations";

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public String getOrganizations() {
        try (Client client = ClientBuilder.newClient()) {
            Response resp = client.target(SPRING_URL)
                    .request()
                    .header("Connection", "close")
                    .get();
            return resp.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "<error>" + ex.getMessage() + "</error>";
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public String getOrganization(@PathParam("id") Long id) { // ← получаем id
        try (Client client = ClientBuilder.newClient()) {
            String targetUrl = SPRING_URL + "/" + id;
            Response resp = client.target(targetUrl)
                    .request()
                    .header("Connection", "close")
                    .get();
            return resp.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "<error>" + ex.getMessage() + "</error>";
        }
    }

}
