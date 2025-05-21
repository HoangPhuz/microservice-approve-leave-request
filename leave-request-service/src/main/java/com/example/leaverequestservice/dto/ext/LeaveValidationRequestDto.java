package com.example.leaverequestservice.dto.ext; // Đặt trong package ext của LRS

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
public class LeaveValidationRequestDto { // Đổi tên từ DTO của LeaveService cho nhất quán
    @NotBlank
    private String employeeId;
    @NotBlank
    private String leaveTypeCode;
    @NotNull
    private Integer forYear;
    @NotNull @FutureOrPresent
    private LocalDate requestedStartDate;
    @NotNull @FutureOrPresent
    private LocalDate requestedEndDate;
//    @NotNull @Positive
//    private BigDecimal requestedDaysInputByEmployee; // Số ngày nhân viên nhập, LeaveService sẽ tính lại ngày làm việc
}