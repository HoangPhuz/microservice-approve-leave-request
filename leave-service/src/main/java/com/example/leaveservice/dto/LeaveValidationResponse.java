package com.example.leaveservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveValidationResponse {
    private boolean isValid; // Yêu cầu có hợp lệ không
    private List<String> validationMessages = new ArrayList<>(); // Lý do nếu không hợp lệ
    private BigDecimal calculatedRemainingDays; // Số ngày nghỉ còn lại thực tế sau khi tính toán
    private BigDecimal calculatedBusinessDaysInRequest; // Số ngày làm việc thực tế trong yêu cầu
}