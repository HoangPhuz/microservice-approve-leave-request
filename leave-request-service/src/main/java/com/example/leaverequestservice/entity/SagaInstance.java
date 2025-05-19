package com.example.leaverequestservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "SagaInstance") // Khớp với database.txt
public class SagaInstance {

    @Id
    @Column(name = "saga_id", length = 36, nullable = false, updatable = false)
    private String sagaId; // UUID_Type

    @Column(name = "saga_type_name", length = 255)
    private String sagaTypeName; // String_Type

    @Column(name = "current_saga_step_name", length = 255)
    private String currentSagaStepName; // String_Type

    @Column(name = "saga_status", length = 255)
    private String sagaStatus; // String_Type, e.g., RUNNING, COMPLETED, COMPENSATING

    @Column(name = "saga_payload_data", columnDefinition = "JSON")
    private String sagaPayloadData; // JSON_Type (lưu trữ dưới dạng String, DB là JSON)

    @Column(name = "correlation_id", length = 36)
    private String correlationId; // UUID_Type (có thể là leave_request_id)

    @CreationTimestamp
    @Column(name = "saga_created_at", nullable = false, updatable = false)
    private LocalDateTime sagaCreatedAt; // Timestamp_Type

    @UpdateTimestamp
    @Column(name = "saga_updated_at", nullable = false)
    private LocalDateTime sagaUpdatedAt; // Timestamp_Type

    @Column(name = "saga_completed_at")
    private LocalDateTime sagaCompletedAt; // Timestamp_Type

    @OneToMany(mappedBy = "sagaInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SagaStepLog> stepLogs;

    @PrePersist
    public void initializeUUID() {
        if (this.sagaId == null) {
            this.sagaId = UUID.randomUUID().toString();
        }
    }
}