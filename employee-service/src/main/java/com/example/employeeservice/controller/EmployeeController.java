package com.example.employeeservice.controller;

import com.example.employeeservice.dto.EmployeeDto;
import com.example.employeeservice.service.EmployeeCrudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeCrudService employeeCrudService;

    public EmployeeController(EmployeeCrudService employeeCrudService) {
        this.employeeCrudService = employeeCrudService;
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable String employeeId) {
        return ResponseEntity.ok(employeeCrudService.getEmployeeDetails(employeeId));
    }

    @GetMapping("/{employeeId}/manager")
    public ResponseEntity<EmployeeDto> getEmployeeManager(@PathVariable String employeeId) {
        return ResponseEntity.ok(employeeCrudService.getEmployeeManagerDetails(employeeId));
    }
    // Thêm các endpoint CRUD khác nếu cần expose ra ngoài
}
