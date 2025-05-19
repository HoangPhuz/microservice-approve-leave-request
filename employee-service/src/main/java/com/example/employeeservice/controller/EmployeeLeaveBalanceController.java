package com.example.employeeservice.controller;

import com.example.employeeservice.dto.EmployeeLeaveBalanceDto;
import com.example.employeeservice.dto.UpdateBalanceAndLogHistoryRequest;
import com.example.employeeservice.service.EmployeeLeaveBalanceManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeLeaveBalanceController {
    private final EmployeeLeaveBalanceManagementService balanceService;

    public EmployeeLeaveBalanceController(EmployeeLeaveBalanceManagementService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/{employeeId}/leave-balances")
    public ResponseEntity<List<EmployeeLeaveBalanceDto>> getLeaveBalances(@PathVariable String employeeId) {
        return ResponseEntity.ok(balanceService.getLeaveBalancesForEmployee(employeeId));
    }

    @GetMapping("/{employeeId}/leave-balances/{leaveTypeCode}")
    public ResponseEntity<EmployeeLeaveBalanceDto> getSpecificLeaveBalance(
            @PathVariable String employeeId,
            @PathVariable String leaveTypeCode,
            @RequestParam int year) {
        return ResponseEntity.ok(balanceService.getSpecificLeaveBalance(employeeId, leaveTypeCode, year));
    }

    // Endpoint này nên được bảo mật và chỉ gọi từ LRS (hoặc xử lý qua event)
    @PostMapping("/internal/balances/update-and-log")
    public ResponseEntity<Void> updateBalanceAndLog(@Valid @RequestBody UpdateBalanceAndLogHistoryRequest request) {
        balanceService.updateBalanceAndLogLeave(request);
        return ResponseEntity.ok().build();
    }
}
