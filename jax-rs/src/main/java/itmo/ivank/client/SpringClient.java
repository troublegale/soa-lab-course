package itmo.ivank.client;

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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;


import java.util.*;

@ApplicationScoped
public class SpringClient {

    private static final String BASE_URL = "https://spring-wildfly:8443/soa/api/v1";
    private final Client client = ClientBuilder.newClient();

    public FireResponse fireAllOrgEmployees(Long id) {
        var ids = getIds(id);

        if (ids.isEmpty()) return new FireResponse(0);

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
            float newTurnover = oldTurnover > Float.MAX_VALUE - acquired.getAnnualTurnover() ?
                    Float.MAX_VALUE :
                    oldTurnover + acquired.getAnnualTurnover();
            compensations.add(() -> updateTurnover(oldTurnover, acquirer));
            var updatedAcquirer = updateTurnover(newTurnover, acquirer);

            var employees = getEmployees(acquiredId);
            compensations.add(() -> transferEmployees(acquiredId, employees));
            var updatedEmployees = transferEmployees(acquirerId, employees);

            compensations.add(() -> compensateOrganization(acquired));
            deleteOrganization(acquiredId);

            return new Acquiring(updatedAcquirer, acquired, updatedEmployees.getEmployees().size());
        } catch (Exception e) {
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
                .request()
                .header("Connection", "close")
                .get()) {
            if (response.getStatus() >= 400) {
                throw new ApiException("Failed to get Employees:\n" + response.readEntity(String.class));
            }
            return response.readEntity(EmployeesList.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("Error during GET to /organizations/" + orgId + "/employees");
        }
    }

    private List<Long> getIds(Long orgId) {
        var employees = getEmployees(orgId);
        return employees.getEmployees().stream().map(Employee::getId).toList();
    }

    private void deleteEmployees(List<Long> ids) {
        var request = new IdRequest(ids);
        try (var response = client.target(BASE_URL + "/employees/batch/delete")
                .request(MediaType.APPLICATION_XML)
                .header("Connection", "close")
                .post(Entity.xml(request))) {
            if (response.getStatus() >= 400) {
                throw new ApiException("Failed to delete Employees:\n" + response.readEntity(String.class));
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception ex) {
            throw new ClientException("Error during POST to /employees/batch/delete");
        }
    }

    private EmployeesList transferEmployees(Long orgId, EmployeesList employees) {
        var request = new EmployeeRequestList(employees.getEmployees()
                .stream().map(e -> new EmployeeRequest(e.getId(), e.getName(), e.getSalary(), orgId))
                .toList());
        try (var response = client.target(BASE_URL + "/employees/batch/update")
                .request(MediaType.APPLICATION_XML)
                .header("Connection", "close")
                .post(Entity.xml(request))) {
            if (response.getStatus() >= 400) {
                throw new ClientException("Failed to transfer Employees:\n" + response.readEntity(String.class));
            }
            return response.readEntity(EmployeesList.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("Error during POST to /employees/batch/update");
        }
    }

    private Organization updateTurnover(Float turnover, Organization organization) {
        try (var response = client.target(BASE_URL + "/organizations/" + organization.getId())
                .request(MediaType.APPLICATION_XML)
                .header("Connection", "close")
                .put(Entity.xml(new OrganizationRequest(
                        organization.getName(),
                        organization.getCoordinates(),
                        turnover,
                        organization.getFullName(),
                        organization.getType(),
                        organization.getOfficialAddress()
                )))) {
            if (response.getStatus() >= 400) {
                throw new ClientException("Failed to update turnover:\n" + response.readEntity(String.class));
            }
            return response.readEntity(Organization.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("Error during PUT to /organizations/" + organization.getId());
        }
    }

    private Organization getOrganization(Long id) {
        try (var response = client.target(BASE_URL + "/organizations/" + id)
                .request()
                .header("Connection", "close")
                .get()) {
            if (response.getStatus() >= 400) {
                throw new ApiException("Failed to get Organization:\n" + response.readEntity(String.class));
            }
            return response.readEntity(Organization.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception ex) {
            throw new ClientException("Error during GET to /organizations/" + id);
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
                .post(Entity.xml(organization))) {
            if (response.getStatus() >= 400) {
                throw new ApiException("Failed to create Organization" + ":\n" + response.readEntity(String.class));
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("Error during POST to /organizations/compensate");
        }
    }

}
