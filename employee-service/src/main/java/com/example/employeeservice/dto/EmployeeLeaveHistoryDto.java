package com.example.employeeservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeLeaveHistoryDto {
    private String historyId; // VARCHAR(36)
    private String employeeId;
    private String originalLeaveRequestId;
    private String leaveTypeCode;
    private String leaveTypeName;
    private LocalDate actualLeaveStartDate; // Date_Type
    private LocalDate actualLeaveEndDate; // Date_Type
    private BigDecimal actualDaysOfLeave; // DECIMAL(10,2)
    private String reasonForApprovedLeave;
    private String requestStatusWhenLogged; // e.g., APPROVED_COMPLETED
    private LocalDateTime historyLoggedAt; // Timestamp_Type
}

