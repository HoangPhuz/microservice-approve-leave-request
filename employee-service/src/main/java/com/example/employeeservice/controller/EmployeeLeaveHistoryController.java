package com.example.employeeservice.controller;

import com.example.employeeservice.dto.EmployeeLeaveHistoryDto;
import com.example.employeeservice.service.EmployeeLeaveHistoryManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees/{employeeId}/leave-history")
public class EmployeeLeaveHistoryController {
    private final EmployeeLeaveHistoryManagementService historyService;

    public EmployeeLeaveHistoryController(EmployeeLeaveHistoryManagementService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeLeaveHistoryDto>> getLeaveHistory(@PathVariable String employeeId) {
        return ResponseEntity.ok(historyService.getLeaveHistoryForEmployee(employeeId));
    }
}
