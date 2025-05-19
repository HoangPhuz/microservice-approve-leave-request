package com.example.approvalservice.controller;



import com.example.approvalservice.dto.CreateLeaveRequestCmd;
import com.example.approvalservice.dto.LeaveRequestDto;
import com.example.approvalservice.dto.UpdateLeaveRequestStatusCmd;
import com.example.approvalservice.service.LeaveRequestManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal/approvals") // Hoặc /api/internal/leave-requests
public class LeaveRequestInternalController {

    private final LeaveRequestManagementService leaveRequestManagementService;

    public LeaveRequestInternalController(LeaveRequestManagementService leaveRequestManagementService) {
        this.leaveRequestManagementService = leaveRequestManagementService;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestDto> createLeaveRequest(@Valid @RequestBody CreateLeaveRequestCmd command) {
        LeaveRequestDto createdRequest = leaveRequestManagementService.createLeaveRequest(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<LeaveRequestDto> updateLeaveRequestStatus(
            @PathVariable String requestId,
            @Valid @RequestBody UpdateLeaveRequestStatusCmd command) {
        LeaveRequestDto updatedRequest = leaveRequestManagementService.updateLeaveRequest(requestId, command);
        return ResponseEntity.ok(updatedRequest);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<LeaveRequestDto> getLeaveRequestById(@PathVariable String requestId) {
        return ResponseEntity.ok(leaveRequestManagementService.getLeaveRequestById(requestId));
    }

    // API này có thể được ManagerService gọi, hoặc LRS cung cấp API tổng hợp cho Manager
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestDto>> getPendingApprovalsForManager(
            @RequestParam("approverId") String approverId,
            @RequestParam(name = "status", defaultValue = "PENDING_MANAGER_APPROVAL") String status) {
        return ResponseEntity.ok(leaveRequestManagementService.getLeaveRequestsByApproverAndStatus(approverId, status));
    }

    // API này có thể được EmployeeService gọi (ít khả năng) hoặc LRS cung cấp API tổng hợp cho Employee
    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<List<LeaveRequestDto>> getLeaveRequestsByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(leaveRequestManagementService.getLeaveRequestsByRequestingEmployee(employeeId));
    }
}