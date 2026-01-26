package itmo.ivank.restadapter.controller;

import itmo.ivank.restadapter.client.SoapCallerClient;
import itmo.ivank.restadapter.dto.FireResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class FireController {

    private final SoapCallerClient client;

    public FireController(SoapCallerClient client) {
        this.client = client;
    }

    @PostMapping(value = "/fire/all/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public FireResponse fireAllOrgEmployees(@PathVariable("id") Long id) {
        return client.fireAllOrgEmployees(id);
    }
}
