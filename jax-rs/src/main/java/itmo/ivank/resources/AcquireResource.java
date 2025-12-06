package itmo.ivank.resources;

import itmo.ivank.client.SpringClient;
import itmo.ivank.dto.Acquiring;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/acquire/{acquirer-id}/{acquired-id}")
public class AcquireResource {

    @Inject
    SpringClient client;

    @POST
    @Produces(MediaType.APPLICATION_XML)
    public Acquiring acquire(@PathParam("acquirer-id") Long acquirerId,
                             @PathParam("acquired-id") Long acquiredId) {
        return client.acquire(acquirerId, acquiredId);
    }

}
