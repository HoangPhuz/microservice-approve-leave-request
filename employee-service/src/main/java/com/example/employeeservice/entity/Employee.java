package com.example.employeeservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "Employee", uniqueConstraints = { // Tên bảng từ file là "Employee"
        @UniqueConstraint(columnNames = {"employee_code"}),
        @UniqueConstraint(columnNames = {"email_address"})
})
public class Employee {

    @Id
    @Column(name = "employee_id", length = 36, nullable = false, updatable = false)
    private String employeeId; // VARCHAR(36) cho UUID

    @Column(name = "employee_code", length = 255, unique = true)
    private String employeeCode;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "email_address", length = 255, unique = true)
    private String emailAddress;

    // Sử dụng String cho department_id vì nó là FK tới bảng Department
    @Column(name = "department_id", length = 36)
    private String departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "department_id", insertable = false, updatable = false)
    private Department department;


    // Sử dụng String cho direct_manager_id vì nó là FK tới chính bảng Employee
    @Column(name = "direct_manager_id", length = 36)
    private String directManagerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direct_manager_id", referencedColumnName = "employee_id", insertable = false, updatable = false)
    private Employee directManager;


    @Column(name = "job_title", length = 255)
    private String jobTitle;

    @Column(name = "hire_date")
    private LocalDate hireDate; // Date_Type

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Timestamp_Type

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // Timestamp_Type

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeLeaveBalance> leaveBalances;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeLeaveHistory> leaveHistories;


    @PrePersist
    public void initializeUUID() {
        if (employeeId == null) {
            employeeId = java.util.UUID.randomUUID().toString();
        }
    }
}