
package com.example.leaveservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeLeaveHistoryDto {
    // Các trường này phải khớp với DTO mà EmployeeService trả về
    private String historyId;
    private String employeeId;
    private String originalLeaveRequestId;
    private String leaveTypeCode;
    private String leaveTypeName;
    private LocalDate actualLeaveStartDate;
    private LocalDate actualLeaveEndDate;
    private BigDecimal actualDaysOfLeave;  // Quan trọng để tính daysTaken
    private String reasonForApprovedLeave;
    private String requestStatusWhenLogged;
    private LocalDateTime historyLoggedAt;
}