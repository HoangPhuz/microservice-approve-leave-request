package com.example.leaverequestservice.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class LeaveRequestDetailsDto {
    private String leaveRequestId; // ID từ ApprovalService
    private String requestingEmployeeId;
    private String requestingEmployeeName; // Lấy từ EmployeeService
    private String leaveTypeCode;
    private String leaveTypeName; // Lấy từ EmployeeService
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal numberOfDays;
    private String reason;
    private String currentStatus; // Từ ApprovalService
    private String assignedApproverId;
    private String assignedApproverName; // Lấy từ EmployeeService
    private LocalDateTime submissionTimestamp;
    private LocalDateTime managerDecisionTimestamp;
    private String managerDecision;
    private String managerRejectionReason;
}