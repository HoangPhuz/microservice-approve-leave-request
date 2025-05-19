package com.example.leaverequestservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "SagaStepLog") // Khớp với database.txt
public class SagaStepLog {

    @Id
    @Column(name = "step_log_id", length = 36, nullable = false, updatable = false)
    private String stepLogId; // UUID_Type

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "saga_id", nullable = false) // Khớp với tên FK trong database.txt
    private SagaInstance sagaInstance;

    @Column(name = "step_name", length = 255)
    private String stepName; // String_Type

    @Column(name = "step_status", length = 255)
    private String stepStatus; // String_Type

    @CreationTimestamp
    @Column(name = "step_execution_timestamp", nullable = false, updatable = false)
    private LocalDateTime stepExecutionTimestamp; // Timestamp_Type

    @Column(name = "step_error_details_or_info", columnDefinition = "TEXT")
    private String stepErrorOrInfoDetails; // Text_Type

    @PrePersist
    public void initializeUUID() {
        if (this.stepLogId == null) {
            this.stepLogId = UUID.randomUUID().toString();
        }
    }
}