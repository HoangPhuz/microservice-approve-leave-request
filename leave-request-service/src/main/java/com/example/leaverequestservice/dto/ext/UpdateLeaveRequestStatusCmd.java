
package com.example.leaverequestservice.dto.ext;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeaveRequestStatusCmd {
    @NotBlank
    private String newStatus;
    private String notes; // Ghi chú chung, hoặc lý do từ chối của quản lý
    private String assignedApproverId; // Có thể cần khi chuyển sang PENDING_MANAGER_APPROVAL
    private String managerDecision; // APPROVED, REJECTED (nếu đây là lệnh cập nhật quyết định)
    private String managerRejectionReason; // Nếu quyết định là REJECTED
    // Không cần truyền toàn bộ LeaveRequest, chỉ những gì cần cập nhật
}