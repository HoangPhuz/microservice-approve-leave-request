package com.example.employeeservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmployeeLeaveBalanceDto {
    private String balanceId;
    private String employeeId;
    private String leaveTypeCode;
    private String leaveTypeName; // Thêm để hiển thị
    private Integer balanceApplicableYear;
    private BigDecimal totalDaysAllocated;
    private LocalDateTime lastBalanceUpdateAt;
}
