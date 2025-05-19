package com.example.approvalservice.dto;



import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeaveRequestStatusCmd {
    @NotBlank
    private String newStatus;
    private String notesOrReason; // Ghi chú chung, hoặc lý do từ chối của quản lý
    private String assignedApproverId; // Gán người duyệt (khi chuyển PENDING_MANAGER_APPROVAL)
    private String managerDecision; // APPROVED, REJECTED
    private String managerRejectionReason; // Nếu quyết định là REJECTED
    private LocalDateTime managerDecisionTimestamp;
}
