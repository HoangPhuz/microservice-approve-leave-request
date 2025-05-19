
package com.example.approvalservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaveRequestCmd {
    @NotBlank
    private String requestingEmployeeId;
    @NotBlank
    private String requestedLeaveTypeCode;
    @NotNull @FutureOrPresent
    private LocalDate plannedStartDate;
    @NotNull @FutureOrPresent
    private LocalDate plannedEndDate;
    @NotNull @Positive
    private BigDecimal requestedNumberOfDays;
    private String reasonForLeaveRequest;
    @NotBlank
    private String initialStatus; // Ví dụ: PENDING_VALIDATION
    private String orchestratingSagaId; // LRS sẽ cung cấp
}