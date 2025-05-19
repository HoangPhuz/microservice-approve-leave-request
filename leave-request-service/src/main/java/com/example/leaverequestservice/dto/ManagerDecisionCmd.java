package com.example.leaverequestservice.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class ManagerDecisionCmd {
    @NotNull
    private DecisionType decision; // Enum: APPROVED, REJECTED
    private String rejectionReason; // Bắt buộc nếu decision là REJECTED
    @NotBlank
    private String decidingManagerId; // Lấy từ token xác thực của quản lý
}
