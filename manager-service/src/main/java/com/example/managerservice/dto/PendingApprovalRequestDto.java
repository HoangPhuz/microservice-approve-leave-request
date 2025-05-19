package com.example.managerservice.dto;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PendingApprovalRequestDto {
    private String requestId;
    private String requestingEmployeeId;
    private String requestingEmployeeName; // Cần lấy từ EmployeeService
    private String leaveTypeName; // Cần lấy từ EmployeeService (thông qua LeaveType của Employee)
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private BigDecimal requestedNumberOfDays;
    private String reasonForLeaveRequest;
    private LocalDateTime requestSubmissionTimestamp;
}