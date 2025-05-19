package com.example.managerservice.dto.ext;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestStatusDto {
    private String requestId;
    private String requestingEmployeeId;
    private String requestedLeaveTypeCode;
    private String assignedApproverId; // Quản lý được gán duyệt
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private BigDecimal requestedNumberOfDays;
    private String reasonForLeaveRequest;
    private String currentRequestStatus;
    private LocalDateTime requestSubmissionTimestamp;
    private LocalDateTime managerDecisionTimestamp;
    private String managerDecision; // APPROVED, REJECTED
    private String managerRejectionReason;
    private String managerReviewNotes;
    // Các trường khác nếu ApprovalService có thêm
}
