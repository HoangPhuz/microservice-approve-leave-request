package com.example.notificationservice.service;



import com.example.notificationservice.dto.SendNotificationCmd;
import com.example.notificationservice.entity.NotificationLog;
import com.example.notificationservice.entity.NotificationTemplate;
import com.example.notificationservice.exception.TemplateNotFoundException;
import com.example.notificationservice.repository.NotificationLogRepository;
import com.example.notificationservice.repository.NotificationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.thymeleaf.TemplateEngine; // Nếu dùng Thymeleaf
// import org.thymeleaf.context.Context; // Nếu dùng Thymeleaf

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class NotificationOrchestrationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationOrchestrationService.class);

    private final NotificationTemplateRepository templateRepository;
    private final NotificationLogRepository logRepository;
    private final EmailSendingService emailSendingService;
    // private final TemplateEngine templateEngine; // Nếu dùng Thymeleaf

    public NotificationOrchestrationService(NotificationTemplateRepository templateRepository,
                                            NotificationLogRepository logRepository,
                                            EmailSendingService emailSendingService
            /*, TemplateEngine templateEngine*/) {
        this.templateRepository = templateRepository;
        this.logRepository = logRepository;
        this.emailSendingService = emailSendingService;
        // this.templateEngine = templateEngine;
    }

    @Transactional // Ghi log và gửi thông báo nên nằm trong cùng transaction (hoặc xử lý bất đồng bộ)
    public void processAndSendNotification(SendNotificationCmd cmd) {
        logger.info("Processing notification for recipient: {}, template: {}", cmd.getRecipientIdentifier(), cmd.getTemplateCode());

        NotificationTemplate template = templateRepository.findByTemplateCodeAndIsActiveTrue(cmd.getTemplateCode())
                .orElseThrow(() -> new TemplateNotFoundException("Active template not found for code: " + cmd.getTemplateCode()));

        String processedSubject = processTemplateString(template.getTemplateSubject(), cmd.getTemplateParameters());
        String processedBody = processTemplateString(template.getTemplateBodyContent(), cmd.getTemplateParameters());

        // Ghi log ban đầu (PENDING)
        NotificationLog log = new NotificationLog();
        log.setRecipientIdentifier(cmd.getRecipientIdentifier());
        log.setNotificationTemplate(template); // Hoặc set templateCodeUsed
        log.setChannelUsed(cmd.getChannel() != null ? cmd.getChannel() : template.getNotificationChannel());
        log.setDeliveryStatus("PENDING");
        log.setRelatedLeaveRequestId(cmd.getRelatedLeaveRequestId());
        log.setRelatedSagaId(cmd.getRelatedSagaId());
        NotificationLog savedLog = logRepository.save(log);

        boolean sentSuccessfully = false;
        String failureReason = null;

        try {
            switch (log.getChannelUsed().toUpperCase()) {
                case "EMAIL":
                    sentSuccessfully = emailSendingService.sendEmail(
                            cmd.getRecipientIdentifier(), // Giả sử đây là email
                            processedSubject,
                            processedBody
                    );
                    break;
                case "SMS":
                    // sentSuccessfully = smsSendingService.sendSms(...);
                    logger.warn("SMS channel not yet implemented for template {}", cmd.getTemplateCode());
                    failureReason = "SMS channel not implemented";
                    break;
                // Các channel khác
                default:
                    logger.warn("Unsupported notification channel: {} for template {}", log.getChannelUsed(), cmd.getTemplateCode());
                    failureReason = "Unsupported channel: " + log.getChannelUsed();
            }
        } catch (Exception e) {
            logger.error("Exception during sending notification {} via {}: {}", savedLog.getLogId(), log.getChannelUsed(), e.getMessage(), e);
            failureReason = e.getMessage();
            sentSuccessfully = false;
        }


        // Cập nhật log sau khi gửi
        if (sentSuccessfully) {
            savedLog.setDeliveryStatus("SENT");
            savedLog.setSentAtTimestamp(LocalDateTime.now());
        } else {
            savedLog.setDeliveryStatus("FAILED");
            savedLog.setDeliveryFailureReason(failureReason != null ? failureReason.substring(0, Math.min(failureReason.length(), 250)) : "Unknown send failure"); // Giới hạn độ dài
        }
        logRepository.save(savedLog);

        if (!sentSuccessfully) {
            // Có thể throw exception ở đây nếu việc gửi thông báo thất bại là nghiêm trọng
            // Hoặc chỉ log và để LRS tiếp tục (tùy theo yêu cầu nghiệp vụ)
            logger.error("Failed to send notification logId: {}", savedLog.getLogId());
        }
    }

    // Hàm xử lý template đơn giản (thay thế placeholder)
    // Nên dùng thư viện template engine như Thymeleaf, Freemarker, Velocity cho email HTML phức tạp
    private String processTemplateString(String templateContent, Map<String, Object> parameters) {
        if (templateContent == null) return "";
        String result = templateContent;
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String placeholder = "\\{\\{\\s*" + entry.getKey() + "\\s*\\}\\}"; // ví dụ: {{requestId}}
                result = result.replaceAll(placeholder, String.valueOf(entry.getValue()));
            }
        }
        return result;
    }

    /* // Ví dụ nếu dùng Thymeleaf:
    private String processThymeleafTemplate(String templateNameInResources, Map<String, Object> parameters) {
        Context context = new Context();
        if (parameters != null) {
            parameters.forEach(context::setVariable);
        }
        return templateEngine.process("email/" + templateNameInResources, context); // Giả sử template ở resources/templates/email/
    }
    */
}