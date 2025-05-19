package com.example.notificationservice.dto;



import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationLogDto {
    private String logId;
    private String relatedLeaveRequestId;
    private String relatedSagaId;
    private String recipientIdentifier;
    private String channelUsed;
    private String templateCodeUsed;
    private String deliveryStatus;
    private LocalDateTime sentAtTimestamp;
    private String deliveryFailureReason;
    private LocalDateTime logCreatedAt;
}