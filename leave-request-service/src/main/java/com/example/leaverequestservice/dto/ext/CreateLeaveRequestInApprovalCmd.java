
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
public class CreateLeaveRequestInApprovalCmd {
    @NotBlank
    private String requestingEmployeeId;
    @NotBlank
    private String leaveTypeCode;
    @NotNull
    private LocalDate plannedStartDate;
    @NotNull
    private LocalDate plannedEndDate;
    @NotNull @Positive
    private BigDecimal requestedNumberOfDays; // Số ngày nhân viên nhập ban đầu
    private String reasonForLeaveRequest;
    @NotBlank
    private String initialStatus; // Ví dụ: PENDING_VALIDATION
    // assignedApproverId sẽ được cập nhật sau khi LRS xác định được quản lý
}
