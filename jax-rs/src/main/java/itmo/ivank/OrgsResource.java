package itmo.ivank;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.net.ssl.SSLContext;

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

}
