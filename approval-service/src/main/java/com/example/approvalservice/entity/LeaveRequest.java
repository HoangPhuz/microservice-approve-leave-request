package com.example.approvalservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "LeaveRequest") // Khớp với database.txt
public class LeaveRequest {

    @Id
    @Column(name = "request_id", length = 36, nullable = false, updatable = false)
    private String requestId; // UUID_Type

    @Column(name = "requesting_employee_id", length = 36, nullable = false)
    private String requestingEmployeeId; // UUID_Type (Logical FK)

    @Column(name = "requested_leave_type_code", length = 50, nullable = false) // Code_Type
    private String requestedLeaveTypeCode; // (Logical FK)

    @Column(name = "assigned_approver_id", length = 36) // UUID_Type (Logical FK)
    private String assignedApproverId; // Quản lý được gán duyệt, LRS sẽ cập nhật

    @Column(name = "orchestrating_saga_id", length = 36) // UUID_Type
    private String orchestratingSagaId; // Để LRS theo dõi

    @Column(name = "planned_start_date", nullable = false)
    private LocalDate plannedStartDate; // Date_Type

    @Column(name = "planned_end_date", nullable = false)
    private LocalDate plannedEndDate; // Date_Type

    @Column(name = "requested_number_of_days", precision = 10, scale = 2)
    private BigDecimal requestedNumberOfDays; // Decimal_Type

    @Column(name = "reason_for_leave_request", columnDefinition = "TEXT")
    private String reasonForLeaveRequest; // Text_Type

    @Column(name = "request_submission_timestamp", nullable = false)
    private LocalDateTime requestSubmissionTimestamp; // Timestamp_Type

    @Column(name = "current_request_status", length = 255, nullable = false)
    private String currentRequestStatus; // String_Type, e.g., PENDING_VALIDATION, PENDING_MANAGER_APPROVAL

    @Column(name = "manager_decision_timestamp")
    private LocalDateTime managerDecisionTimestamp; // Timestamp_Type

    @Column(name = "manager_decision", length = 255) // String_Type
    private String managerDecision; // e.g., APPROVED, REJECTED

    @Column(name = "manager_rejection_reason", columnDefinition = "TEXT")
    private String managerRejectionReason; // Text_Type

    @Column(name = "manager_review_notes", columnDefinition = "TEXT")
    private String managerReviewNotes; // Text_Type

    @CreationTimestamp
    @Column(name = "request_created_at", nullable = false, updatable = false)
    private LocalDateTime requestCreatedAt; // Timestamp_Type

    @UpdateTimestamp
    @Column(name = "request_updated_at", nullable = false)
    private LocalDateTime requestUpdatedAt; // Timestamp_Type

    @PrePersist
    public void initializeUUIDAndTimestamp() {
        if (this.requestId == null) {
            this.requestId = UUID.randomUUID().toString();
        }
        if (this.requestSubmissionTimestamp == null) {
            this.requestSubmissionTimestamp = LocalDateTime.now();
        }
    }
}