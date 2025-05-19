package com.example.managerservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ManagedEmployeeDto {
    private String employeeId;
    private String employeeCode;
    private String fullName;
    private String emailAddress;
    private String jobTitle;
    private String departmentName; // Lấy từ EmployeeDetailsDto
}