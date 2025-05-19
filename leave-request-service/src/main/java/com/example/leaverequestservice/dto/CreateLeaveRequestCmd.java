package com.example.leaverequestservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class CreateLeaveRequestCmd {
    @NotBlank
    private String requestingEmployeeId; // Lấy từ token xác thực
    @NotBlank
    private String leaveTypeCode;
    @NotNull @FutureOrPresent
    private LocalDate startDate;
    @NotNull @FutureOrPresent
    private LocalDate endDate;
    @NotNull @Positive
    private BigDecimal numberOfDays; // Số ngày nhân viên nghĩ họ đang xin
    @Size(max = 1000)
    private String reason;
}