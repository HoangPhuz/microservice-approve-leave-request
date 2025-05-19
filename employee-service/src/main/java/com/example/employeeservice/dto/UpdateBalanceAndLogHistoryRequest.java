package com.example.employeeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateBalanceAndLogHistoryRequest {
    @NotBlank
    private String employeeId;
    @NotBlank
    private String leaveTypeCode;
    @NotNull
    private Integer applicableYear;
    @NotNull @Positive
    private BigDecimal daysToDeduct;
    @NotBlank
    private String originalLeaveRequestId;
    private String approvedReason;
    @NotNull
    private LocalDate actualLeaveStartDate;
    @NotNull
    private LocalDate actualLeaveEndDate;
}
