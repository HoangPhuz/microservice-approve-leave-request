package com.example.employeeservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
public class EmployeeDto {
    private String employeeId;
    private String employeeCode;
    private String fullName;
    private String emailAddress;
    private String departmentId;
    private String departmentName; // Có thể thêm nếu cần trả về tên phòng ban
    private String directManagerId;
    private String directManagerName; // Có thể thêm nếu cần trả về tên quản lý
    private String jobTitle;
    private LocalDate hireDate;

}