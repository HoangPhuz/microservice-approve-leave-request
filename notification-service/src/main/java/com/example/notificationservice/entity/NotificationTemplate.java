package com.example.notificationservice.entity;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "NotificationTemplate") // Khớp với database.txt
public class NotificationTemplate {

    @Id
    @Column(name = "template_id", length = 36, nullable = false, updatable = false)
    private String templateId; // UUID_Type

    @Column(name = "template_code", length = 50, nullable = false, unique = true) // Code_Type
    private String templateCode; // e.g., LEAVE_SUBMITTED_TO_MANAGER

    @Column(name = "notification_channel", length = 255, nullable = false) // String_Type
    private String notificationChannel; // EMAIL, SMS, IN_APP

    @Column(name = "template_subject", columnDefinition = "TEXT") // Text_Type
    private String templateSubject; // Tiêu đề cho email

    @Column(name = "template_body_content", columnDefinition = "TEXT", nullable = false) // Text_Type
    private String templateBodyContent; // Nội dung mẫu (có thể chứa placeholder)

    @Column(name = "template_language_code", length = 50, nullable = false) // Code_Type
    private String templateLanguageCode; // e.g., "vi", "en"

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Boolean_Type

    @PrePersist
    public void initializeUUID() {
        if (this.templateId == null) {
            this.templateId = UUID.randomUUID().toString();
        }
    }
}