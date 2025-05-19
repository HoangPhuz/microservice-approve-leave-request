
package com.example.leaveservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LeaveValidationRequest {
    @NotBlank
    private String employeeId;
    @NotBlank
    private String leaveTypeCode;
    @NotNull
    private Integer forYear; // Năm của kỳ nghỉ được yêu cầu
    @NotNull @FutureOrPresent
    private LocalDate requestedStartDate;
    @NotNull @FutureOrPresent
    private LocalDate requestedEndDate;
    @NotNull @Positive
    private BigDecimal requestedDays; // Số ngày yêu cầu (đã được tính toán cơ bản bởi LRS)
}