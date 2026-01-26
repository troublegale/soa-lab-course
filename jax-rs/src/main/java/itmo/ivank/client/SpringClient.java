package itmo.ivank.client;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import itmo.ivank.dto.Acquiring;
import itmo.ivank.dto.FireResponse;
import itmo.ivank.dto.IdRequest;
import itmo.ivank.dto.employee.Employee;
import itmo.ivank.dto.employee.EmployeesList;
import itmo.ivank.dto.employee.EmployeeRequest;
import itmo.ivank.dto.employee.EmployeeRequestList;
import itmo.ivank.dto.organization.Organization;
import itmo.ivank.dto.organization.OrganizationRequest;
import itmo.ivank.exception.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.math.BigDecimal;
import java.util.*;

@ApplicationScoped
public class SpringClient {

    private static final String BASE_URL = "https://called-web:8443/soa/api/v1";
    private Client client;
    private XmlMapper xmlMapper;

    @PostConstruct
    public void init() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }}, new java.security.SecureRandom());
            
            xmlMapper = new XmlMapper();
            xmlMapper.registerModule(new JakartaXmlBindAnnotationModule());
            
            client = ClientBuilder.newBuilder()
                    .sslContext(sslContext)
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SSL context", e);
        }
    }
    
    private <T> T readXml(String xml, Class<T> clazz) {
        try {
            return xmlMapper.readValue(xml, clazz);
        } catch (Exception e) {
            throw new ClientException("Failed to parse XML: " + e.getMessage());
        }
    }
    
    private String writeXml(Object obj) {
        try {
            return xmlMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ClientException("Failed to write XML: " + e.getMessage());
        }
    }

    public FireResponse fireAllOrgEmployees(Long id) {
        var ids = getIds(id);

        if (ids == null || ids.isEmpty()) return new FireResponse(0);

        deleteEmployees(ids);
        return new FireResponse(ids.size());
    }

    public Acquiring acquire(Long acquirerId, Long acquiredId) {
        if (Objects.equals(acquirerId, acquiredId)) throw new ClientException("Organization can not acquire itself");

        List<Runnable> compensations = new ArrayList<>();

        try {
            var acquirer = getOrganization(acquirerId);
            var acquired = getOrganization(acquiredId);

            float oldTurnover = acquirer.getAnnualTurnover();
            BigDecimal newTurnoverBD = BigDecimal.valueOf(oldTurnover)
                    .add(BigDecimal.valueOf(acquired.getAnnualTurnover()));
            float newTurnover = newTurnoverBD.floatValue();
            compensations.add(() -> updateTurnover(oldTurnover, acquirer));
            var updatedAcquirer = updateTurnover(newTurnover, acquirer);

            var employees = getEmployees(acquiredId);
            var number = 0;
            if (employees.getEmployees() != null) {
                compensations.add(() -> transferEmployees(acquiredId, employees));
                number = transferEmployees(acquirerId, employees).getEmployees().size();
            }

            compensations.add(() -> compensateOrganization(acquired));
            deleteOrganization(acquiredId);

            return new Acquiring(updatedAcquirer, acquired, number);
        } catch (Exception e) {
            e.printStackTrace();
            StringBuilder message = new StringBuilder("Acquiring failed:" + e.getMessage());
            if (!compensations.isEmpty()) {
                message.append("\nPerforming ").append(compensations.size()).append(" compensation(s)");
                for (int i = compensations.size() - 1; i >= 0; i--) {
                    try {
                        compensations.get(i).run();
                        message.append("\nCompensation").append(i).append(" succeeded");
                    } catch (ClientException ce) {
                        message.append("\nCompensation ").append(i).append(" failed: ").append(ce.getMessage());
                    }
                }
            }
            if (e instanceof ClientException) {
                throw new ServiceException("Exception due to internal logic or network:\n" + message);
            } else if (e instanceof ApiException) {
                throw new ServiceException("Exception due to API communication error:\n" + message);
            } else throw new ServiceException("Unknown error:\n" + message);
        }
    }

    private EmployeesList getEmployees(Long orgId) {
        try (var response = client.target(BASE_URL + "/organizations/" + orgId + "/employees?size=0")
                .request(MediaType.APPLICATION_XML)
                .header("Connection", "close")
                .get()) {
            String xml = response.readEntity(String.class);
            if (response.getStatus() >= 400) {
                throw new ApiException("Failed to get Employees:\n" + xml);
            }
            return readXml(xml, EmployeesList.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("Error during GET to /organizations/" + orgId + "/employees: " + e.getMessage());
        }
    }

    private List<Long> getIds(Long orgId) {
        var employees = getEmployees(orgId);
        if (employees.getEmployees() == null) return null;
        return employees.getEmployees().stream().map(Employee::getId).toList();
    }

    private void deleteEmployees(List<Long> ids) {
        try (var response = client.target(BASE_URL + "/employees/batch/delete")
                .request(MediaType.APPLICATION_XML)
                .header("Connection", "close")
                .post(Entity.entity(writeXml(ids), MediaType.APPLICATION_XML))) {
            if (response.getStatus() >= 400) {
                throw new ApiException("Failed to delete Employees:\n" + response.readEntity(String.class));
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception ex) {
            throw new ClientException("Error during POST to /employees/batch/delete: " + ex.getMessage());
        }
    }

    private EmployeesList transferEmployees(Long orgId, EmployeesList employees) {
        var request = new EmployeeRequestList(employees.getEmployees()
                .stream().map(e -> new EmployeeRequest(e.getId(), e.getName(), e.getSalary(), orgId))
                .toList());
        try (var response = client.target(BASE_URL + "/employees/batch/update")
                .request(MediaType.APPLICATION_XML)
                .header("Connection", "close")
                .post(Entity.entity(writeXml(request), MediaType.APPLICATION_XML))) {
            String xml = response.readEntity(String.class);
            if (response.getStatus() >= 400) {
                throw new ClientException("Failed to transfer Employees:\n" + xml);
            }
            return readXml(xml, EmployeesList.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("Error during POST to /employees/batch/update: " + e.getMessage());
        }
    }

    private Organization updateTurnover(Float turnover, Organization organization) {
        var address = organization.getOfficialAddress() == null || organization.getOfficialAddress().getStreet() == null ?
                null : organization.getOfficialAddress();
        var requestObj = new OrganizationRequest(
                organization.getName(),
                organization.getCoordinates(),
                turnover,
                organization.getFullName(),
                organization.getType(),
                address
        );
        try (var response = client.target(BASE_URL + "/organizations/" + organization.getId())
                .request(MediaType.APPLICATION_XML)
                .header("Connection", "close")
                .put(Entity.entity(writeXml(requestObj), MediaType.APPLICATION_XML))) {
            String xml = response.readEntity(String.class);
            if (response.getStatus() >= 400) {
                throw new ClientException("Failed to update turnover:\n" + xml);
            }
            return readXml(xml, Organization.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("Error during PUT to /organizations/" + organization.getId() + ": " + e.getMessage());
        }
    }

    private Organization getOrganization(Long id) {
        try (var response = client.target(BASE_URL + "/organizations/" + id)
                .request(MediaType.APPLICATION_XML)
                .header("Connection", "close")
                .get()) {
            String xml = response.readEntity(String.class);
            if (response.getStatus() >= 400) {
                throw new ApiException("Failed to get Organization:\n" + xml);
            }
            return readXml(xml, Organization.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ClientException("Error during GET to /organizations/" + id + ": " + ex.getMessage());
        }
    }

    private void deleteOrganization(Long id) {
        try (var response = client.target(BASE_URL + "/organizations/" + id)
                .request()
                .header("Connection", "close")
                .delete()) {
            if (response.getStatus() >= 400) {
                throw new ApiException("Failed to delete Organization #" + id + ":\n" +
                        response.readEntity(String.class));
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("Error during DELETE to /organizations/" + id);
        }
    }

    private void compensateOrganization(Organization organization) {
        try (var response = client.target(BASE_URL + "/organizations/compensate")
                .request(MediaType.APPLICATION_XML)
                .header("Connection", "close")
                .post(Entity.entity(writeXml(organization), MediaType.APPLICATION_XML))) {
            if (response.getStatus() >= 400) {
                throw new ApiException("Failed to create Organization" + ":\n" + response.readEntity(String.class));
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("Error during POST to /organizations/compensate: " + e.getMessage());
        }
    }

}
