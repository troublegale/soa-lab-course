package itmo.ivank.resources;

import itmo.ivank.client.SpringClient;
import itmo.ivank.dto.FireResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path("/fire/all/{id}")
public class FireResource {

    @Inject
    SpringClient client;

    @POST
    @Produces(MediaType.APPLICATION_XML)
    public FireResponse fireAllOrgEmployees(@PathParam("id") Long id) {
        return client.fireAllOrgEmployees(id);
    }

}
