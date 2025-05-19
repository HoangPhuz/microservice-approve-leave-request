
package com.example.leaverequestservice.dto.ext;

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
public class UpdateBalanceAndLogHistoryCmd {
    @NotBlank
    private String employeeId;
    @NotBlank
    private String leaveTypeCode;
    @NotNull
    private Integer applicableYear; // Năm áp dụng cho số dư
    @NotNull @Positive
    private BigDecimal daysToDeduct; // Số ngày làm việc thực tế đã được LeaveService tính
    @NotBlank
    private String originalLeaveRequestId; // ID của yêu cầu nghỉ gốc từ ApprovalService
    private String approvedReason; // Lý do nghỉ đã được duyệt
    @NotNull
    private LocalDate actualLeaveStartDate;
    @NotNull
    private LocalDate actualLeaveEndDate;
}