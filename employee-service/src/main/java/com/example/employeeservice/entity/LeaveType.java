package com.example.employeeservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "LeaveType") // Tên bảng từ file
public class LeaveType {
    @Id
    @Column(name = "leave_type_code", length = 50, nullable = false, updatable = false)
    private String leaveTypeCode; // VARCHAR(50)

    @Column(name = "leave_type_name", length = 255)
    private String leaveTypeName;

    @Column(name = "basic_policy_description", columnDefinition = "TEXT")
    private String basicPolicyDescription;
}
