package com.example.managerservice.controller;

import com.example.managerservice.dto.ManagedEmployeeDto;
import com.example.managerservice.dto.PendingApprovalRequestDto;
import com.example.managerservice.service.ManagerDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager") // Hoặc /api/managers
public class ManagerController {

    private final ManagerDashboardService managerDashboardService;

    public ManagerController(ManagerDashboardService managerDashboardService) {
        this.managerDashboardService = managerDashboardService;
    }

    // API để quản lý lấy danh sách nhân viên cấp dưới của mình
    @GetMapping("/{managerId}/managed-employees")
    public ResponseEntity<List<ManagedEmployeeDto>> getManagedEmployees(@PathVariable String managerId) {
        // TODO: Thêm kiểm tra xem người gọi có phải là managerId hoặc admin không
        return ResponseEntity.ok(managerDashboardService.getManagedEmployees(managerId));
    }

    // API để quản lý lấy danh sách các yêu cầu nghỉ phép đang chờ mình duyệt
    @GetMapping("/{managerId}/pending-leave-requests")
    public ResponseEntity<List<PendingApprovalRequestDto>> getPendingLeaveRequests(@PathVariable String managerId) {
        // TODO: Thêm kiểm tra xem người gọi có phải là managerId hoặc admin không
        return ResponseEntity.ok(managerDashboardService.getPendingLeaveRequestsForManager(managerId));
    }

    // Các API khác liên quan đến nghiệp vụ của quản lý có thể được thêm vào đây
    // Ví dụ: API để quản lý xem lịch sử phê duyệt của mình,
    // hoặc API để quản lý thực hiện các hành động cụ thể (nhưng việc Phê duyệt/Từ chối
    // thường vẫn do LRS xử lý dựa trên request từ client của quản lý).
}