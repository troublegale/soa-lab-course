package itmo.ivank.web.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service Discovery client for Consul using simple HTTP client.
 * Discovers EJB service instances from Consul registry.
 */
public class ConsulServiceDiscovery {

    private static final Logger logger = Logger.getLogger(ConsulServiceDiscovery.class.getName());

    private static final String CONSUL_HOST = System.getenv().getOrDefault("CONSUL_HOST", "consul");
    private static final int CONSUL_PORT = Integer.parseInt(System.getenv().getOrDefault("CONSUL_PORT", "8500"));

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Service endpoint information
     */
    public static class ServiceEndpoint {
        private final String host;
        private final int port;

        public ServiceEndpoint(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return host + ":" + port;
        }
    }

    /**
     * Discover a healthy service instance by name.
     * Returns the first healthy instance found.
     *
     * @param serviceName the name of the service to discover
     * @return Optional containing the service endpoint if found
     */
    public static Optional<ServiceEndpoint> discoverService(String serviceName) {
        try {
            String consulUrl = String.format("http://%s:%d/v1/health/service/%s?passing=true",
                    CONSUL_HOST, CONSUL_PORT, serviceName);

            logger.info("Querying Consul for service: " + serviceName + " at " + consulUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(consulUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.warning("Consul returned status " + response.statusCode());
                return Optional.empty();
            }

            JsonNode services = objectMapper.readTree(response.body());

            if (services.isEmpty()) {
                logger.warning("No healthy instances found for service: " + serviceName);
                return Optional.empty();
            }

            // Get first healthy instance
            JsonNode firstService = services.get(0);
            JsonNode serviceNode = firstService.get("Service");

            String address = serviceNode.get("Address").asText();
            int port = serviceNode.get("Port").asInt();

            // If address is empty, use node address
            if (address == null || address.isEmpty()) {
                address = firstService.get("Node").get("Address").asText();
            }

            // EJB uses HTTP port 8080, not HTTPS 8443
            // Service is registered with port 8443, but EJB remote uses 8080
            int ejbPort = 8080;

            logger.info("Discovered service " + serviceName + " at: " + address + ":" + ejbPort);
            return Optional.of(new ServiceEndpoint(address, ejbPort));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to discover service: " + serviceName, e);
            return Optional.empty();
        }
    }

    /**
     * Discover the called-ejb service.
     * Falls back to default if Consul is unavailable.
     *
     * @return ServiceEndpoint for the EJB service
     */
    public static ServiceEndpoint discoverEjbService() {
        return discoverService("called-ejb")
                .orElseGet(() -> {
                    logger.warning("Falling back to default EJB endpoint: called-ejb:8080");
                    return new ServiceEndpoint("called-ejb", 8080);
                });
    }
}
