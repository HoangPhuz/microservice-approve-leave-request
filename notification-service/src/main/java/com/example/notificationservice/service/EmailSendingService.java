package com.example.notificationservice.service;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailSendingService {
    private static final Logger logger = LoggerFactory.getLogger(EmailSendingService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String mailFrom;

    public boolean sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true for multipart
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates html
            mailSender.send(message);
            logger.info("Email sent successfully to {}", to);
            return true;
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }
}