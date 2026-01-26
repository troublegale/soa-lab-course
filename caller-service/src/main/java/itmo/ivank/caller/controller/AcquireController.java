package itmo.ivank.caller.controller;

import itmo.ivank.caller.client.CalledServiceClient;
import itmo.ivank.caller.dto.Acquiring;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AcquireController {

    private final CalledServiceClient client;

    public AcquireController(CalledServiceClient client) {
        this.client = client;
    }

    @PostMapping(value = "/acquire/{acquirer-id}/{acquired-id}", produces = MediaType.APPLICATION_XML_VALUE)
    public Acquiring acquire(@PathVariable("acquirer-id") Long acquirerId,
                             @PathVariable("acquired-id") Long acquiredId) {
        return client.acquire(acquirerId, acquiredId);
    }
}
