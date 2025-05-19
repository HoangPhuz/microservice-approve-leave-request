package com.example.employeeservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "EmployeeLeaveHistory") // Tên bảng từ file
public class EmployeeLeaveHistory {
    @Id
    @Column(name = "history_id", length = 36, nullable = false, updatable = false)
    private String historyId; // VARCHAR(36)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // original_leave_request_id là một tham chiếu logic, lưu dưới dạng String (UUID)
    @Column(name = "original_leave_request_id", length = 36)
    private String originalLeaveRequestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_code", nullable = false)
    private LeaveType leaveType;

    @Column(name = "actual_leave_start_date")
    private LocalDate actualLeaveStartDate; // Date_Type

    @Column(name = "actual_leave_end_date")
    private LocalDate actualLeaveEndDate; // Date_Type

    @Column(name = "actual_days_of_leave", precision = 10, scale = 2)
    private BigDecimal actualDaysOfLeave; // DECIMAL(10,2)

    @Column(name = "reason_for_approved_leave", columnDefinition = "TEXT")
    private String reasonForApprovedLeave;

    @Column(name = "request_status_when_logged", length = 255)
    private String requestStatusWhenLogged; // e.g., APPROVED_COMPLETED

    @Column(name = "history_logged_at", nullable = false, updatable = false)
    @CreationTimestamp // Sẽ tự động gán khi tạo
    private LocalDateTime historyLoggedAt; // Timestamp_Type

    @PrePersist
    public void initializeUUID() {
        if (historyId == null) {
            historyId = UUID.randomUUID().toString();
        }
    }
}
