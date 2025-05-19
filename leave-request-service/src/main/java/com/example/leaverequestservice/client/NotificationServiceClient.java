

package com.example.leaverequestservice.client;

import com.example.leaverequestservice.dto.ext.SendNotificationCmd; // DTO cho NotificationService
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/api/notifications")
public interface NotificationServiceClient {

    @PostMapping("/send")
    ResponseEntity<Void> sendNotification(@RequestBody SendNotificationCmd command);
}
