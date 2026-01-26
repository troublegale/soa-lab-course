package itmo.ivank.restadapter.controller;

import itmo.ivank.restadapter.client.SoapCallerClient;
import itmo.ivank.restadapter.dto.Acquiring;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AcquireController {

    private final SoapCallerClient client;

    public AcquireController(SoapCallerClient client) {
        this.client = client;
    }

    @PostMapping(value = "/acquire/{acquirer-id}/{acquired-id}", produces = MediaType.APPLICATION_XML_VALUE)
    public Acquiring acquire(@PathVariable("acquirer-id") Long acquirerId,
                             @PathVariable("acquired-id") Long acquiredId) {
        return client.acquire(acquirerId, acquiredId);
    }
}
