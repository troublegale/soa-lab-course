package itmo.ivank;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(("/yes"))
public class TestResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String yes() {
        return "yes";
    }

}
