package itmo.ivank.soa.controller;

import itmo.ivank.soa.dto.*;
import itmo.ivank.soa.entity.Organization;
import itmo.ivank.soa.service.OrganizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public OrganizationsPage getAllOrganizations(@RequestParam(defaultValue = "1") @Valid Integer page,
                                                 @RequestParam(defaultValue = "20") @Valid Integer size) {
        return organizationService.getAll(page, size);
    }

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public Organization createOrganization(@RequestBody @Valid @NotNull OrganizationRequest request) {
        return organizationService.create(request);
    }

    @PostMapping(path = "/query", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public OrganizationsPage getFilteredOrganizations(@RequestParam(defaultValue = "1") @Valid Integer page,
                                                      @RequestParam(defaultValue = "20") @Valid Integer size,
                                                      @RequestBody @Valid @NotNull OrganizationQuery query) {
        return organizationService.getFiltered(page, size, query);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public Organization getOrganization(@PathVariable @Valid Long id) {
        return organizationService.getById(id);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public Organization updateOrganization(@PathVariable @Valid Long id, @RequestBody @Valid @NotNull OrganizationRequest request) {
        return organizationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteOrganization(@PathVariable @Valid Long id) {
        organizationService.delete(id);
    }

    @GetMapping(path = "/{id}/employees", produces = MediaType.APPLICATION_XML_VALUE)
    public EmployeesList getOrganizationEmployees(@PathVariable @Valid Long id) {
        return organizationService.getEmployees(id);
    }

    @GetMapping(path = "/turnover", produces = MediaType.APPLICATION_XML_VALUE)
    public TurnoverResponse getTotalTurnover() {
        return organizationService.getTotalTurnover();
    }

    @GetMapping(path = "/types", produces = MediaType.APPLICATION_XML_VALUE)
    public TypeCountResponse getOrganizationTypesCount() {
        return organizationService.getOrganizationTypesCount();
    }

    @PostMapping(path = "/lt-full-name", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public OrganizationsPage getOrganizationsLessThanFullName(@RequestBody @NotNull FullNameValue fullNameValue,
                                                              @RequestParam(defaultValue = "1") @Valid Integer page,
                                                              @RequestParam(defaultValue = "20") @Valid Integer size) {
        return organizationService.getOrganizationsLessThanFullName(fullNameValue.value(), page, size);
    }

    @PostMapping(path = "/compensate", consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public Organization compensateOrganization(@RequestBody @Valid @NotNull OrganizationRequest request) {
        return organizationService.createRaw(request);
    }

}
