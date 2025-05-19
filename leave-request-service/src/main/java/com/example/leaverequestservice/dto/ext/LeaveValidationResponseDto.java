
package com.example.leaverequestservice.dto.ext; // Đặt trong package ext của LRS

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveValidationResponseDto { // Đổi tên từ DTO của LeaveService cho nhất quán
    private boolean isValid;
    private List<String> validationMessages = new ArrayList<>();
    private BigDecimal calculatedRemainingDays;
    private BigDecimal calculatedBusinessDaysInRequest;
}
