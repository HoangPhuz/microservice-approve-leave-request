package com.example.notificationservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "NotificationLog") // Khớp với database.txt
public class NotificationLog {

    @Id
    @Column(name = "log_id", length = 36, nullable = false, updatable = false)
    private String logId; // UUID_Type

    @Column(name = "related_leave_request_id", length = 36) // UUID_Type
    private String relatedLeaveRequestId; // Logical FK

    @Column(name = "related_saga_id", length = 36) // UUID_Type
    private String relatedSagaId; // Logical FK

    @Column(name = "recipient_identifier", length = 255, nullable = false) // String_Type
    private String recipientIdentifier; // e.g., email address, phone, user_id

    @Column(name = "channel_used", length = 255, nullable = false) // String_Type
    private String channelUsed;

    @ManyToOne(fetch = FetchType.LAZY) // Hoặc chỉ lưu template_code_used dạng String nếu không cần join thường xuyên
    @JoinColumn(name = "template_code_used", referencedColumnName = "template_code", nullable = false) // Code_Type
    private NotificationTemplate notificationTemplate;
    // Hoặc nếu chỉ lưu code:
    // @Column(name = "template_code_used", length = 50, nullable = false)
    // private String templateCodeUsed;


    @Column(name = "delivery_status", length = 255, nullable = false) // String_Type
    private String deliveryStatus; // e.g., SENT, FAILED, PENDING_RETRY

    @Column(name = "sent_at_timestamp")
    private LocalDateTime sentAtTimestamp; // Timestamp_Type

    @Column(name = "delivery_failure_reason", columnDefinition = "TEXT") // Text_Type
    private String deliveryFailureReason;

    @CreationTimestamp
    @Column(name = "log_created_at", nullable = false, updatable = false)
    private LocalDateTime logCreatedAt; // Timestamp_Type

    @PrePersist
    public void initializeUUID() {
        if (this.logId == null) {
            this.logId = UUID.randomUUID().toString();
        }
    }
}