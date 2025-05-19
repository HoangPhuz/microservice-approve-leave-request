package com.example.leaveservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmployeeLeaveBalanceDto {
    // Các trường này phải khớp với DTO mà EmployeeService trả về
    private String balanceId;
    private String employeeId;
    private String leaveTypeCode;
    private String leaveTypeName;
    private Integer balanceApplicableYear;
    private BigDecimal totalDaysAllocated;
    private LocalDateTime lastBalanceUpdateAt;
}