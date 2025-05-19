

package com.example.leaverequestservice.client;

import com.example.leaverequestservice.dto.ext.CreateLeaveRequestInApprovalCmd; // DTO cho ApprovalService
import com.example.leaverequestservice.dto.ext.LeaveRequestStatusDto; // DTO cho ApprovalService
import com.example.leaverequestservice.dto.ext.UpdateLeaveRequestStatusCmd; // DTO cho ApprovalService
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Giả sử ApprovalService có các API này
@FeignClient(name = "approval-service", path = "/api/internal/approvals") // path có thể là /api/leave-requests
public interface ApprovalServiceClient {

    @PostMapping // POST /api/internal/approvals
    ResponseEntity<LeaveRequestStatusDto> createLeaveRequest(@RequestBody CreateLeaveRequestInApprovalCmd command);

    @PutMapping("/{requestId}/status") // PUT /api/internal/approvals/{requestId}/status
    ResponseEntity<LeaveRequestStatusDto> updateLeaveRequestStatus(
            @PathVariable("requestId") String requestId,
            @RequestBody UpdateLeaveRequestStatusCmd command);

    @GetMapping("/{requestId}")
    ResponseEntity<LeaveRequestStatusDto> getLeaveRequestStatus(@PathVariable("requestId") String requestId);
}