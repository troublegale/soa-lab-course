package itmo.ivank.client;

import itmo.ivank.dto.Acquiring;
import itmo.ivank.dto.FireResponse;
import itmo.ivank.dto.employee.Employee;
import itmo.ivank.dto.employee.EmployeeRequest;
import itmo.ivank.dto.employee.EmployeesResponse;
import itmo.ivank.dto.organization.Organization;
import itmo.ivank.dto.organization.OrganizationRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;

@ApplicationScoped
public class SpringClient {

    private static final String BASE_URL = "https://spring-wildfly:8443/soa/api/v1";
    private final Client client = ClientBuilder.newClient();

    public FireResponse fireAllOrgEmployees(Long id) {
        List<EmployeeRequest> compensations = new ArrayList<>();
        try {
            Response resp = client.target(BASE_URL + "/organizations/" + id + "/employees")
                    .request()
                    .header("Connection", "close")
                    .get();
            var employees = resp.readEntity(EmployeesResponse.class).getEmployees();
            if (employees == null || employees.isEmpty()) return new FireResponse(0);
            var ids = employees.stream().map(Employee::getId).toList();
            for (int i = 0; i < employees.size(); i++) {
                compensations.add(new EmployeeRequest(
                        employees.get(i).getName(),
                        employees.get(i).getSalary(),
                        employees.get(i).getOrganization().getId()
                ));
                client.target(BASE_URL + "/employees/" + ids.get(i))
                        .request()
                        .header("Connection", "close")
                        .delete();
            }
            return new FireResponse(employees.size());
        } catch (Exception e) {
            for (var emp : compensations) {
                client.target(BASE_URL + "/employees")
                        .request(MediaType.APPLICATION_XML)
                        .header("Connection", "close")
                        .post(Entity.xml(emp));
            }
            throw new RuntimeException(e);
        }
    }

    public Acquiring acquire(Long acquirerId, Long acquiredId) {
        if (Objects.equals(acquirerId, acquiredId)) {
            throw new RuntimeException("Can't acquire self (id=id");
        }
        OrganizationRequest acquirerCompensation = null;
        OrganizationRequest acquiredCompensation = null;
        Map<Long, EmployeeRequest> employeeCompensations = new HashMap<>();
        try {
            Response resp = client.target(BASE_URL + "/organizations/" + acquirerId)
                    .request()
                    .header("Connection", "close")
                    .get();
            Organization acquirer = resp.readEntity(Organization.class);
            resp = client.target(BASE_URL + "/organizations/" + acquiredId)
                    .request()
                    .header("Connection", "close")
                    .get();
            Organization acquired = resp.readEntity(Organization.class);
            acquirerCompensation = new OrganizationRequest(
                    acquirer.getName(),
                    acquirer.getCoordinates(),
                    acquirer.getAnnualTurnover(),
                    acquirer.getFullName(),
                    acquirer.getType(),
                    acquirer.getOfficialAddress()
            );
            Float turnover = acquirer.getAnnualTurnover();
            if (turnover > Float.MAX_VALUE - acquired.getAnnualTurnover()) {
                turnover = Float.MAX_VALUE;
            } else {
                turnover += acquired.getAnnualTurnover();
            }
            acquirer.setAnnualTurnover(turnover);
            resp = client.target(BASE_URL + "/organizations/" + acquirerId)
                    .request(MediaType.APPLICATION_XML)
                    .header("Connection", "close")
                    .put(Entity.xml(new OrganizationRequest(
                            acquirer.getName(),
                            acquirer.getCoordinates(),
                            acquirer.getAnnualTurnover(),
                            acquirer.getFullName(),
                            acquirer.getType(),
                            acquirer.getOfficialAddress()
                    )));
            acquirer = resp.readEntity(Organization.class);
            resp = client.target(BASE_URL + "/organizations/" + acquiredId + "/employees")
                    .request()
                    .header("Connection", "close")
                    .get();
            var employees = resp.readEntity(EmployeesResponse.class).getEmployees();
            if (employees != null) {
                for (var e : employees) {
                    employeeCompensations.put(e.getId(), new EmployeeRequest(e.getName(), e.getSalary(), e.getOrganization().getId()));
                    client.target(BASE_URL + "/employees/" + e.getId())
                            .request(MediaType.APPLICATION_XML)
                            .header("Connection", "close")
                            .put(Entity.xml(new EmployeeRequest(e.getName(), e.getSalary(), acquirer.getId())));
                }
            }
            acquiredCompensation = new OrganizationRequest(
                    acquired.getName(),
                    acquired.getCoordinates(),
                    acquired.getAnnualTurnover(),
                    acquired.getFullName(),
                    acquired.getType(),
                    acquired.getOfficialAddress()
            );
            client.target(BASE_URL + "/organizations/" + acquiredId)
                    .request()
                    .header("Connection", "close")
                    .delete();
            return new Acquiring(acquirer, acquired, employees == null ? 0 : employees.size());
        } catch (Exception e) {
            if (acquiredCompensation != null) {
                client.target(BASE_URL + "/organizations")
                        .request(MediaType.APPLICATION_XML)
                        .header("Connection", "close")
                        .post(Entity.xml(acquiredCompensation));
            }
            for (var id : employeeCompensations.keySet()) {
                client.target(BASE_URL + "/employees/" + id)
                        .request(MediaType.APPLICATION_XML)
                        .header("Connection", "close")
                        .put(Entity.xml(employeeCompensations.get(id)));
            }
            if (acquirerCompensation != null) {
                client.target(BASE_URL + "/organizations/" + acquirerId)
                        .request(MediaType.APPLICATION_XML)
                        .header("Connection", "close")
                        .put(Entity.xml(acquirerCompensation));
            }
            throw new RuntimeException(e);
        }
    }

}
