package com.example.notificationservice.repository;

import com.example.notificationservice.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {
    Optional<NotificationTemplate> findByTemplateCodeAndIsActiveTrue(String templateCode);
}
