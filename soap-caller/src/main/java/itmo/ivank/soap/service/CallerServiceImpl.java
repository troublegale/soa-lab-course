package itmo.ivank.soap.service;

import itmo.ivank.soap.client.MuleServiceClient;
import itmo.ivank.soap.dto.*;
import itmo.ivank.soap.exception.CallerServiceException;
import itmo.ivank.soap.exception.CallerServiceFault;

import javax.jws.WebService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@WebService(
    serviceName = "CallerServiceService",
    portName = "CallerServicePort",
    endpointInterface = "itmo.ivank.soap.service.CallerService",
    targetNamespace = "http://soap.ivank.itmo/caller"
)
public class CallerServiceImpl implements CallerService {

    private final MuleServiceClient client;

    public CallerServiceImpl() {
        this.client = new MuleServiceClient();
    }

    @Override
    public FireResponse fireAllOrgEmployees(Long organizationId) throws CallerServiceException {
        try {
            List<Long> ids = getEmployeeIds(organizationId);

            if (ids == null || ids.isEmpty()) {
                return new FireResponse(0);
            }

            deleteEmployees(ids);
            return new FireResponse(ids.size());
        } catch (CallerServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new CallerServiceException("Error firing employees: " + e.getMessage(), 
                new CallerServiceFault(500, e.getMessage()));
        }
    }

    @Override
    public Acquiring acquire(Long acquirerId, Long acquiredId) throws CallerServiceException {
        if (Objects.equals(acquirerId, acquiredId)) {
            throw new CallerServiceException("Organization cannot acquire itself", 
                new CallerServiceFault(400, "Organization cannot acquire itself"));
        }

        List<Runnable> compensations = new ArrayList<>();

        try {
            Organization acquirer = getOrganization(acquirerId);
            Organization acquired = getOrganization(acquiredId);

            float oldTurnover = acquirer.getAnnualTurnover();
            BigDecimal newTurnoverBD = BigDecimal.valueOf(oldTurnover)
                    .add(BigDecimal.valueOf(acquired.getAnnualTurnover()));
            float newTurnover = newTurnoverBD.floatValue();
            
            final float compensateTurnover = oldTurnover;
            final Organization compensateAcquirer = acquirer;
            compensations.add(() -> {
                try {
                    updateTurnover(compensateTurnover, compensateAcquirer);
                } catch (Exception ignored) {}
            });
            Organization updatedAcquirer = updateTurnover(newTurnover, acquirer);

            EmployeesList employees = getEmployees(acquiredId);
            int number = 0;
            if (employees.getEmployees() != null && !employees.getEmployees().isEmpty()) {
                final Long compensateOrgId = acquiredId;
                final EmployeesList compensateEmployees = employees;
                compensations.add(() -> {
                    try {
                        transferEmployees(compensateOrgId, compensateEmployees);
                    } catch (Exception ignored) {}
                });
                EmployeesList transferred = transferEmployees(acquirerId, employees);
                number = transferred.getEmployees() != null ? transferred.getEmployees().size() : 0;
            }

            final Organization compensateOrg = acquired;
            compensations.add(() -> {
                try {
                    compensateOrganization(compensateOrg);
                } catch (Exception ignored) {}
            });
            deleteOrganization(acquiredId);

            return new Acquiring(updatedAcquirer, acquired, number);
        } catch (CallerServiceException e) {
            performCompensations(compensations);
            throw e;
        } catch (Exception e) {
            performCompensations(compensations);
            throw new CallerServiceException("Acquiring failed: " + e.getMessage(), 
                new CallerServiceFault(500, e.getMessage()));
        }
    }

    private void performCompensations(List<Runnable> compensations) {
        for (int i = compensations.size() - 1; i >= 0; i--) {
            try {
                compensations.get(i).run();
            } catch (Exception ignored) {}
        }
    }

    private EmployeesList getEmployees(Long orgId) throws CallerServiceException {
        return client.getEmployees(orgId);
    }

    private List<Long> getEmployeeIds(Long orgId) throws CallerServiceException {
        EmployeesList employees = getEmployees(orgId);
        if (employees.getEmployees() == null) {
            return null;
        }
        return employees.getEmployees().stream()
                .map(Employee::getId)
                .collect(Collectors.toList());
    }

    private void deleteEmployees(List<Long> ids) throws CallerServiceException {
        client.deleteEmployees(ids);
    }

    private EmployeesList transferEmployees(Long orgId, EmployeesList employees) throws CallerServiceException {
        List<EmployeeRequest> requests = employees.getEmployees().stream()
                .map(e -> new EmployeeRequest(e.getId(), e.getName(), e.getSalary(), orgId))
                .collect(Collectors.toList());
        EmployeeRequestList requestList = new EmployeeRequestList(requests);
        return client.updateEmployees(requestList);
    }

    private Organization updateTurnover(Float turnover, Organization organization) throws CallerServiceException {
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
        return client.updateOrganization(organization.getId(), requestObj);
    }

    private Organization getOrganization(Long id) throws CallerServiceException {
        return client.getOrganization(id);
    }

    private void deleteOrganization(Long id) throws CallerServiceException {
        client.deleteOrganization(id);
    }

    private void compensateOrganization(Organization organization) throws CallerServiceException {
        client.compensateOrganization(organization);
    }
}
