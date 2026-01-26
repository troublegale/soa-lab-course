package itmo.ivank.caller.controller;

import itmo.ivank.caller.client.CalledServiceClient;
import itmo.ivank.caller.dto.FireResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class FireController {

    private final CalledServiceClient client;

    public FireController(CalledServiceClient client) {
        this.client = client;
    }

    @PostMapping(value = "/fire/all/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public FireResponse fireAllOrgEmployees(@PathVariable("id") Long id) {
        return client.fireAllOrgEmployees(id);
    }
}
