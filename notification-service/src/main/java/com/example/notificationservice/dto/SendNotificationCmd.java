package com.example.notificationservice.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationCmd {
    @NotBlank
    private String recipientIdentifier; // e.g., email address, user_id
    @NotBlank
    private String templateCode;
    @NotEmpty
    private Map<String, Object> templateParameters; // Dữ liệu để điền vào template
    private String channel; // (Tùy chọn) Kênh cụ thể: EMAIL, SMS, IN_APP. Nếu null, service tự quyết định.
    private String relatedLeaveRequestId; // (Tùy chọn) Để log
    private String relatedSagaId; // (Tùy chọn) Để log
}