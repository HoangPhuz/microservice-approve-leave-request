package com.example.employeeservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "EmployeeLeaveBalance", uniqueConstraints = { // Tên bảng từ file
        @UniqueConstraint(columnNames = {"employee_id", "leave_type_code", "balance_applicable_year"})
})
public class EmployeeLeaveBalance {
    @Id
    @Column(name = "balance_id", length = 36, nullable = false, updatable = false)
    private String balanceId; // VARCHAR(36)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_code", nullable = false)
    private LeaveType leaveType;

    @Column(name = "balance_applicable_year", nullable = false)
    private Integer balanceApplicableYear; // INT

    @Column(name = "total_days_allocated", precision = 10, scale = 2)
    private BigDecimal totalDaysAllocated; // DECIMAL(10,2)

    @Column(name = "last_balance_update_at")
    private LocalDateTime lastBalanceUpdateAt; // Timestamp_Type

    @PrePersist
    public void initializeUUID() {
        if (balanceId == null) {
            balanceId = UUID.randomUUID().toString();
        }
    }



}
