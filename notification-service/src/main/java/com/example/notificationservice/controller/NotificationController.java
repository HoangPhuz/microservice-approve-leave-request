package com.example.notificationservice.controller;



import com.example.notificationservice.dto.SendNotificationCmd;
import com.example.notificationservice.service.NotificationOrchestrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications") // API này thường là nội bộ, được LRS gọi
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationOrchestrationService orchestrationService;

    public NotificationController(NotificationOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@Valid @RequestBody SendNotificationCmd command) {
        try {
            // Việc gửi có thể bất đồng bộ (ví dụ: đưa vào queue nội bộ của NotificationService)
            // Hoặc đồng bộ như hiện tại. Nếu LRS không cần đợi kết quả gửi thành công ngay:
            orchestrationService.processAndSendNotification(command);
            return ResponseEntity.accepted().build(); // Hoặc OK() nếu LRS cần biết đã xử lý xong
        } catch (Exception e) {
            logger.error("Error processing send notification command: {}", command, e);
            // Trả về lỗi để LRS có thể biết (tùy theo thiết kế Saga)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}