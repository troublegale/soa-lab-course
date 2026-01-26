package itmo.ivank.soap.client;

import itmo.ivank.soap.dto.*;
import itmo.ivank.soap.exception.CallerServiceException;
import itmo.ivank.soap.exception.CallerServiceFault;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * HTTP Client for communicating with Mule ESB Integration Bus.
 * Mule ESB acts as an intermediary between this SOAP service and the Spring REST service.
 * Uses JAXB for XML serialization (compatible with WildFly).
 */
public class MuleServiceClient {

    private final String baseUrl;
    private final CloseableHttpClient httpClient;

    public MuleServiceClient() {
        // Read Mule ESB URL from environment or use default
        String muleHost = System.getenv("MULE_ESB_HOST");
        String mulePort = System.getenv("MULE_ESB_PORT");
        
        if (muleHost == null || muleHost.isEmpty()) {
            muleHost = "mule-esb";
        }
        if (mulePort == null || mulePort.isEmpty()) {
            mulePort = "8082";
        }
        
        this.baseUrl = "http://" + muleHost + ":" + mulePort + "/api/v1";
        this.httpClient = HttpClients.createDefault();
        
        System.out.println("[MuleServiceClient] Initialized with baseUrl: " + this.baseUrl);
    }

    private String writeXml(Object obj) throws CallerServiceException {
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new CallerServiceException("Failed to serialize XML: " + e.getMessage(),
                new CallerServiceFault(500, e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T readXml(String xml, Class<T> clazz) throws CallerServiceException {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new CallerServiceException("Failed to parse XML: " + e.getMessage() + ", xml=" + xml,
                new CallerServiceFault(500, e.getMessage()));
        }
    }

    public Organization getOrganization(Long id) throws CallerServiceException {
        try {
            HttpGet request = new HttpGet(baseUrl + "/organizations/" + id);
            request.setHeader("Accept", "application/xml");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());
                
                if (statusCode >= 400) {
                    throw new CallerServiceException("Failed to get organization: " + body,
                        new CallerServiceFault(statusCode, body));
                }
                
                if (body == null || body.isBlank()) {
                    throw new CallerServiceException("Empty response for organization #" + id,
                        new CallerServiceFault(404, "Organization not found"));
                }
                
                return readXml(body, Organization.class);
            }
        } catch (CallerServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new CallerServiceException("Error getting organization: " + e.getMessage(),
                new CallerServiceFault(500, e.getMessage()));
        }
    }

    public Organization updateOrganization(Long id, OrganizationRequest request) throws CallerServiceException {
        try {
            HttpPut httpRequest = new HttpPut(baseUrl + "/organizations/" + id);
            httpRequest.setHeader("Content-Type", "application/xml");
            httpRequest.setHeader("Accept", "application/xml");
            httpRequest.setEntity(new StringEntity(writeXml(request), "UTF-8"));
            
            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());
                
                if (statusCode >= 400) {
                    throw new CallerServiceException("Failed to update organization: " + body,
                        new CallerServiceFault(statusCode, body));
                }
                
                return readXml(body, Organization.class);
            }
        } catch (CallerServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new CallerServiceException("Error updating organization: " + e.getMessage(),
                new CallerServiceFault(500, e.getMessage()));
        }
    }

    public void deleteOrganization(Long id) throws CallerServiceException {
        try {
            HttpDelete request = new HttpDelete(baseUrl + "/organizations/" + id);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                
                if (statusCode >= 400) {
                    String body = EntityUtils.toString(response.getEntity());
                    throw new CallerServiceException("Failed to delete organization: " + body,
                        new CallerServiceFault(statusCode, body));
                }
            }
        } catch (CallerServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new CallerServiceException("Error deleting organization: " + e.getMessage(),
                new CallerServiceFault(500, e.getMessage()));
        }
    }

    public void compensateOrganization(Organization organization) throws CallerServiceException {
        try {
            HttpPost request = new HttpPost(baseUrl + "/organizations/compensate");
            request.setHeader("Content-Type", "application/xml");
            request.setHeader("Accept", "application/xml");
            request.setEntity(new StringEntity(writeXml(organization), "UTF-8"));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                
                if (statusCode >= 400) {
                    String body = EntityUtils.toString(response.getEntity());
                    throw new CallerServiceException("Failed to compensate organization: " + body,
                        new CallerServiceFault(statusCode, body));
                }
            }
        } catch (CallerServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new CallerServiceException("Error compensating organization: " + e.getMessage(),
                new CallerServiceFault(500, e.getMessage()));
        }
    }

    public EmployeesList getEmployees(Long orgId) throws CallerServiceException {
        try {
            HttpGet request = new HttpGet(baseUrl + "/organizations/" + orgId + "/employees?size=0");
            request.setHeader("Accept", "application/xml");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());
                
                if (statusCode >= 400) {
                    throw new CallerServiceException("Failed to get employees: " + body,
                        new CallerServiceFault(statusCode, body));
                }
                
                // Handle empty employees response
                if (body == null || body.isBlank() || 
                    body.equals("<employees/>") || body.equals("<employees></employees>")) {
                    return new EmployeesList(null);
                }
                
                return readXml(body, EmployeesList.class);
            }
        } catch (CallerServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new CallerServiceException("Error getting employees: " + e.getMessage(),
                new CallerServiceFault(500, e.getMessage()));
        }
    }

    public EmployeesList updateEmployees(EmployeeRequestList requestList) throws CallerServiceException {
        try {
            HttpPost request = new HttpPost(baseUrl + "/employees/batch/update");
            request.setHeader("Content-Type", "application/xml");
            request.setHeader("Accept", "application/xml");
            request.setEntity(new StringEntity(writeXml(requestList), "UTF-8"));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());
                
                if (statusCode >= 400) {
                    throw new CallerServiceException("Failed to update employees: " + body,
                        new CallerServiceFault(statusCode, body));
                }
                
                return readXml(body, EmployeesList.class);
            }
        } catch (CallerServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new CallerServiceException("Error updating employees: " + e.getMessage(),
                new CallerServiceFault(500, e.getMessage()));
        }
    }

    public void deleteEmployees(List<Long> ids) throws CallerServiceException {
        try {
            HttpPost request = new HttpPost(baseUrl + "/employees/batch/delete");
            request.setHeader("Content-Type", "application/xml");
            
            IdList idList = new IdList(ids);
            request.setEntity(new StringEntity(writeXml(idList), "UTF-8"));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                
                if (statusCode >= 400) {
                    String body = EntityUtils.toString(response.getEntity());
                    throw new CallerServiceException("Failed to delete employees: " + body,
                        new CallerServiceFault(statusCode, body));
                }
            }
        } catch (CallerServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new CallerServiceException("Error deleting employees: " + e.getMessage(),
                new CallerServiceFault(500, e.getMessage()));
        }
    }
}
