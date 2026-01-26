package itmo.ivank.restadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RestAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestAdapterApplication.class, args);
    }
}
