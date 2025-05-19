
package com.example.managerservice.client;

import com.example.managerservice.dto.ext.LeaveRequestStatusDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// Giả sử ApprovalService có API này (hoặc LRS expose API này)
@FeignClient(name = "approval-service", path = "/api/internal/approvals") // Hoặc "leave-request-service" nếu LRS cung cấp API này
public interface ApprovalServiceClient {

    // API để lấy các yêu cầu đang chờ duyệt của một quản lý cụ thể
    @GetMapping("/pending")
    ResponseEntity<List<LeaveRequestStatusDto>> getPendingApprovalsForManager(
            @RequestParam("approverId") String managerId,
            @RequestParam(name = "status", defaultValue = "PENDING_MANAGER_APPROVAL") String status
    );
}
