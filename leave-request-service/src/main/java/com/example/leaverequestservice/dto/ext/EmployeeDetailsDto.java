
package com.example.leaverequestservice.dto.ext;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailsDto {
    private String employeeId;
    private String employeeCode;
    private String fullName;
    private String emailAddress;
    private String departmentId;
    private String departmentName; // LRS có thể cần tên phòng ban
    private String directManagerId; // QUAN TRỌNG: LRS cần để biết ai duyệt
    private String directManagerName; // LRS có thể cần tên quản lý
    private String jobTitle;
    private LocalDate hireDate;
    // Thêm các trường khác của Employee nếu LRS cần
}