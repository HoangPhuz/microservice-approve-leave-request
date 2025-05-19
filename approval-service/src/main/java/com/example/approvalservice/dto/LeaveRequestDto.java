package com.example.approvalservice.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDto {
    private String requestId;
    private String requestingEmployeeId;
    private String requestedLeaveTypeCode;
    private String assignedApproverId;
    private String orchestratingSagaId;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private BigDecimal requestedNumberOfDays;
    private String reasonForLeaveRequest;
    private LocalDateTime requestSubmissionTimestamp;
    private String currentRequestStatus;
    private LocalDateTime managerDecisionTimestamp;
    private String managerDecision;
    private String managerRejectionReason;
    private String managerReviewNotes;
    private LocalDateTime requestCreatedAt;
    private LocalDateTime requestUpdatedAt;
}