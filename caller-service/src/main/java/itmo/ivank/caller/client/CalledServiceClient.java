package itmo.ivank.caller.client;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import itmo.ivank.caller.dto.*;
import itmo.ivank.caller.exception.ApiException;
import itmo.ivank.caller.exception.ClientException;
import itmo.ivank.caller.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CalledServiceClient {

    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;
    private final String baseUrl;

    public CalledServiceClient(RestTemplate restTemplate,
                               XmlMapper xmlMapper,
                               @Value("${called-service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.xmlMapper = xmlMapper;
        this.baseUrl = baseUrl;
    }

    private HttpHeaders createXmlHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setAccept(List.of(MediaType.APPLICATION_XML));
        return headers;
    }

    private String writeXml(Object obj) {
        try {
            return xmlMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ClientException("Failed to write XML: " + e.getMessage());
        }
    }

    private <T> T readXml(String xml, Class<T> clazz) {
        try {
            return xmlMapper.readValue(xml, clazz);
        } catch (Exception e) {
            throw new ClientException("Failed to parse XML: " + e.getMessage());
        }
    }

    public FireResponse fireAllOrgEmployees(Long id) {
        List<Long> ids = getEmployeeIds(id);

        if (ids == null || ids.isEmpty()) {
            return new FireResponse(0);
        }

        deleteEmployees(ids);
        return new FireResponse(ids.size());
    }

    public Acquiring acquire(Long acquirerId, Long acquiredId) {
        if (Objects.equals(acquirerId, acquiredId)) {
            throw new ClientException("Organization can not acquire itself");
        }

        List<Runnable> compensations = new ArrayList<>();

        try {
            Organization acquirer = getOrganization(acquirerId);
            Organization acquired = getOrganization(acquiredId);

            float oldTurnover = acquirer.getAnnualTurnover();
            BigDecimal newTurnoverBD = BigDecimal.valueOf(oldTurnover)
                    .add(BigDecimal.valueOf(acquired.getAnnualTurnover()));
            float newTurnover = newTurnoverBD.floatValue();
            compensations.add(() -> updateTurnover(oldTurnover, acquirer));
            Organization updatedAcquirer = updateTurnover(newTurnover, acquirer);

            EmployeesList employees = getEmployees(acquiredId);
            int number = 0;
            if (employees.getEmployees() != null && !employees.getEmployees().isEmpty()) {
                compensations.add(() -> transferEmployees(acquiredId, employees));
                number = transferEmployees(acquirerId, employees).getEmployees().size();
            }

            compensations.add(() -> compensateOrganization(acquired));
            deleteOrganization(acquiredId);

            return new Acquiring(updatedAcquirer, acquired, number);
        } catch (Exception e) {
            e.printStackTrace();
            StringBuilder message = new StringBuilder("Acquiring failed: " + e.getMessage());
            if (!compensations.isEmpty()) {
                message.append("\nPerforming ").append(compensations.size()).append(" compensation(s)");
                for (int i = compensations.size() - 1; i >= 0; i--) {
                    try {
                        compensations.get(i).run();
                        message.append("\nCompensation ").append(i).append(" succeeded");
                    } catch (ClientException ce) {
                        message.append("\nCompensation ").append(i).append(" failed: ").append(ce.getMessage());
                    }
                }
            }
            if (e instanceof ClientException) {
                throw new ServiceException("Exception due to internal logic or network:\n" + message);
            } else if (e instanceof ApiException) {
                throw new ServiceException("Exception due to API communication error:\n" + message);
            } else {
                throw new ServiceException("Unknown error:\n" + message);
            }
        }
    }

    private EmployeesList getEmployees(Long orgId) {
        try {
            String url = baseUrl + "/organizations/" + orgId + "/employees?size=0";
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createXmlHeaders()),
                    String.class
            );
            String body = response.getBody();
            // Handle empty employees response
            if (body == null || body.isBlank() || body.equals("<employees/>") || body.equals("<employees></employees>")) {
                return new EmployeesList(null);
            }
            return readXml(body, EmployeesList.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ApiException("Failed to get Employees: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ClientException("Error during GET to /organizations/" + orgId + "/employees: " + e.getMessage());
        }
    }

    private List<Long> getEmployeeIds(Long orgId) {
        EmployeesList employees = getEmployees(orgId);
        if (employees.getEmployees() == null) {
            return null;
        }
        return employees.getEmployees().stream().map(Employee::getId).collect(Collectors.toList());
    }

    private void deleteEmployees(List<Long> ids) {
        try {
            String url = baseUrl + "/employees/batch/delete";
            String xml = writeXml(ids);
            HttpEntity<String> entity = new HttpEntity<>(xml, createXmlHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
                throw new ApiException("Failed to delete Employees: " + response.getBody());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ApiException("Failed to delete Employees: " + e.getResponseBodyAsString());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("Error during POST to /employees/batch/delete: " + e.getMessage());
        }
    }

    private EmployeesList transferEmployees(Long orgId, EmployeesList employees) {
        EmployeeRequestList request = new EmployeeRequestList(employees.getEmployees()
                .stream()
                .map(e -> new EmployeeRequest(e.getId(), e.getName(), e.getSalary(), orgId))
                .collect(Collectors.toList()));
        try {
            String url = baseUrl + "/employees/batch/update";
            String xml = writeXml(request);
            HttpEntity<String> entity = new HttpEntity<>(xml, createXmlHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return readXml(response.getBody(), EmployeesList.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ClientException("Failed to transfer Employees: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ClientException("Error during POST to /employees/batch/update: " + e.getMessage());
        }
    }

    private Organization updateTurnover(Float turnover, Organization organization) {
        Address address = organization.getOfficialAddress() == null ||
                organization.getOfficialAddress().getStreet() == null ?
                null : organization.getOfficialAddress();
        OrganizationRequest requestObj = new OrganizationRequest(
                organization.getName(),
                organization.getCoordinates(),
                turnover,
                organization.getFullName(),
                organization.getType(),
                address
        );
        try {
            String url = baseUrl + "/organizations/" + organization.getId();
            String xml = writeXml(requestObj);
            HttpEntity<String> entity = new HttpEntity<>(xml, createXmlHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            return readXml(response.getBody(), Organization.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ClientException("Failed to update turnover: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ClientException("Error during PUT to /organizations/" + organization.getId() + ": " + e.getMessage());
        }
    }

    private Organization getOrganization(Long id) {
        try {
            String url = baseUrl + "/organizations/" + id;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createXmlHeaders()),
                    String.class
            );
            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new ClientException("Empty response for organization #" + id);
            }
            return readXml(body, Organization.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ApiException("Failed to get Organization: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ClientException("Error during GET to /organizations/" + id + ": " + e.getMessage());
        }
    }

    private void deleteOrganization(Long id) {
        try {
            String url = baseUrl + "/organizations/" + id;
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(createXmlHeaders()), String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ApiException("Failed to delete Organization #" + id + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ClientException("Error during DELETE to /organizations/" + id);
        }
    }

    private void compensateOrganization(Organization organization) {
        try {
            String url = baseUrl + "/organizations/compensate";
            String xml = writeXml(organization);
            HttpEntity<String> entity = new HttpEntity<>(xml, createXmlHeaders());
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ApiException("Failed to create Organization: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ClientException("Error during POST to /organizations/compensate: " + e.getMessage());
        }
    }
}
