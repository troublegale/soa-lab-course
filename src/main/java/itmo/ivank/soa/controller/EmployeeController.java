package itmo.ivank.soa.controller;

import itmo.ivank.soa.dto.EmployeeRequest;
import itmo.ivank.soa.entity.Employee;
import itmo.ivank.soa.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE,
    produces = MediaType.APPLICATION_XML_VALUE)
    public Employee createEmployee(@RequestBody @Valid @NotNull EmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public Employee updateEmployee(@PathVariable Long id, @RequestBody @Valid @NotNull EmployeeRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public Employee getEmployee(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    @DeleteMapping(path = "/{id}")
    void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteById(id);
    }

}
