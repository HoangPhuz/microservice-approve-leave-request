
package com.example.leaverequestservice.dto.ext;

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
    private String templateCode; // Mã của mẫu thông báo
    @NotEmpty
    private Map<String, Object> templateParameters; // Các tham số để điền vào mẫu
    private String channel; // (Tùy chọn) Kênh cụ thể EMAIL, SMS, IN_APP. Nếu null, NotificationService tự quyết định.

    private String relatedLeaveRequestId;
    private String relatedSagaId;
}