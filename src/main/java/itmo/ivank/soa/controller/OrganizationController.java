package itmo.ivank.soa.controller;

import itmo.ivank.soa.dto.OrganizationQuery;
import itmo.ivank.soa.dto.OrganizationRequest;
import itmo.ivank.soa.dto.OrganizationTypesResponse;
import itmo.ivank.soa.dto.OrganizationsPage;
import itmo.ivank.soa.entity.Employee;
import itmo.ivank.soa.entity.Organization;
import itmo.ivank.soa.service.OrganizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    OrganizationsPage getAllOrganizations() {
        return null;
    }

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    Organization createOrganization(@RequestBody @Valid @NotNull OrganizationRequest request) {
        return null;
    }

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    OrganizationsPage getFilteredOrganizations(@RequestBody @Valid @NotNull OrganizationQuery query) {
        return null;
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    Organization getOrganization(@PathVariable Long id) {
        return null;
    }

    @PutMapping(path = "/{id}",  consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    Organization updateOrganization(@PathVariable Long id, @RequestBody @Valid @NotNull OrganizationRequest request) {
        return null;
    }

    @DeleteMapping("/{id}")
    void deleteOrganization(@PathVariable Long id) {

    }

    @GetMapping(path = "/{id}/employees", produces = MediaType.APPLICATION_XML_VALUE)
    List<Employee> getOrganizationEmployees(@PathVariable Long id) {
        return null;
    }

    @GetMapping(path = "/turnover", produces = MediaType.APPLICATION_XML_VALUE)
    Map<String, Float> getTotalTurnover() {
        return null;
    }

    @GetMapping(path = "/types", produces = MediaType.APPLICATION_XML_VALUE)
    OrganizationTypesResponse getOrganizationTypesCount() {
        return null;
    }

    @PostMapping(path = "/lt-full-name",  consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    OrganizationsPage getOrganizationsLessThanFullName(@RequestBody @NotNull String value) {
        return null;
    }

}
