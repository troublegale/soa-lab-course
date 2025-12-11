package itmo.ivank.soa.controller;

import itmo.ivank.soa.dto.EmployeeRequest;
import itmo.ivank.soa.dto.EmployeesList;
import itmo.ivank.soa.entity.Employee;
import itmo.ivank.soa.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public Employee createEmployee(@RequestBody @Valid @NotNull EmployeeRequest request) {
        return employeeService.create(request);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public Employee updateEmployee(@PathVariable Long id, @RequestBody @Valid @NotNull EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public Employee getEmployee(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    @DeleteMapping(path = "/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteById(id);
    }

    @PostMapping(path = "/batch/create", consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public EmployeesList createBatch(@RequestBody @Valid @NotNull List<EmployeeRequest> employees) {
        return employeeService.createBatch(employees);
    }

    @PostMapping(path = "/batch/update", consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public EmployeesList updateBatch(@RequestBody @Valid @NotNull List<EmployeeRequest> employees) {
        return employeeService.updateBatch(employees);
    }

    @PostMapping(path = "/batch/delete", consumes = MediaType.APPLICATION_XML_VALUE)
    public void deleteBatch(@RequestBody @Valid @NotNull List<Long> ids) {
        employeeService.deleteBatch(ids);
    }

}
