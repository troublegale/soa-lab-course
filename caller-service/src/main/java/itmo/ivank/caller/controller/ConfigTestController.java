package itmo.ivank.caller.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
@RequestMapping("/api/v1")
public class ConfigTestController {

    @Value("${app.message:Default message}")
    private String message;

    @Value("${called-service.url}")
    private String calledServiceUrl;

    @GetMapping(value = "/config-test", produces = MediaType.APPLICATION_XML_VALUE)
    public String getConfigValues() {
        return "<config>" +
                "<message>" + message + "</message>" +
                "<calledServiceUrl>" + calledServiceUrl + "</calledServiceUrl>" +
                "</config>";
    }
}
