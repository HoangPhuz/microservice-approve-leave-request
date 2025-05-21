package com.example.leaverequestservice.service;

import com.example.leaverequestservice.client.*;
import com.example.leaverequestservice.dto.CreateLeaveRequestCmd;
import com.example.leaverequestservice.dto.DecisionType;
import com.example.leaverequestservice.dto.ManagerDecisionCmd;
import com.example.leaverequestservice.dto.ext.*;
import com.example.leaverequestservice.dto.saga.LeaveRequestSagaPayload;
import com.example.leaverequestservice.entity.SagaInstance;
import com.example.leaverequestservice.exception.ResourceNotFoundException;
import com.example.leaverequestservice.repository.SagaInstanceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects; // Thêm import này

@Service
public class LeaveRequestSagaOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestSagaOrchestrator.class);

    private final SagaStateService sagaStateService;
    private final EmployeeServiceClient employeeServiceClient;
    private final ApprovalServiceClient approvalServiceClient;
    private final LeaveServiceClient leaveServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final SagaInstanceRepository sagaInstanceRepository; // Đã có
    private final ObjectMapper objectMapper;

    public LeaveRequestSagaOrchestrator(SagaStateService sagaStateService,
                                        EmployeeServiceClient employeeServiceClient,
                                        ApprovalServiceClient approvalServiceClient,
                                        LeaveServiceClient leaveServiceClient,
                                        NotificationServiceClient notificationServiceClient,
                                        SagaInstanceRepository sagaInstanceRepository, // Thêm vào constructor
                                        ObjectMapper objectMapper) {
        this.sagaStateService = sagaStateService;
        this.employeeServiceClient = employeeServiceClient;
        this.approvalServiceClient = approvalServiceClient;
        this.leaveServiceClient = leaveServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.sagaInstanceRepository = sagaInstanceRepository; // Gán
        this.objectMapper = objectMapper;
    }

    // Wrapper cho sagaStateService.createSaga để xử lý payload object
    private SagaInstance createSagaInternal(String sagaType, String correlationId, LeaveRequestSagaPayload payload) {
        try {
            String payloadStr = objectMapper.writeValueAsString(payload);
            return sagaStateService.createSaga(sagaType, correlationId, payloadStr);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize saga payload during creation: {}", e.getMessage());
            // Xử lý lỗi, có thể ném exception hoặc tạo saga với payload lỗi
            return sagaStateService.createSaga(sagaType, correlationId, "{\"error\":\"Payload serialization failed\"}");
        }
    }

    // Wrapper cho sagaStateService.updateSagaState để xử lý payload object
    private void updateSagaStateInternal(String sagaId, String currentStep, String status, String errorDetails, LeaveRequestSagaPayload payload) {
        try {
            String payloadStr = (payload != null) ? objectMapper.writeValueAsString(payload) : null;
            sagaStateService.updateSagaState(sagaId, currentStep, status, errorDetails, payloadStr);
        } catch (JsonProcessingException e) {
            logger.error("Saga {}: Failed to serialize saga payload for step {}: {}", sagaId, currentStep, e.getMessage());
            // Cập nhật với payload lỗi nếu không serialize được, nhưng vẫn ghi log lỗi
            sagaStateService.updateSagaState(sagaId, currentStep, status, errorDetails, "{\"error\":\"Payload serialization failed\"}");
        }
    }


    @Transactional
    public String startLeaveRequestSaga(CreateLeaveRequestCmd cmd) {
        String requestingEmployeeId = cmd.getRequestingEmployeeId();

        LeaveRequestSagaPayload sagaPayload = new LeaveRequestSagaPayload();
        sagaPayload.setCreateLeaveRequestCmd(cmd);

        SagaInstance saga = createSagaInternal(
                "LeaveApprovalSaga",
                null, // CorrelationId sẽ là requestId từ ApprovalService
                sagaPayload
        );
        logger.info("Saga {} created for employee {}", saga.getSagaId(), requestingEmployeeId);

        try {
            updateSagaStateInternal(saga.getSagaId(), "CREATE_LEAVE_REQUEST_IN_APPROVAL", "RUNNING", null, sagaPayload);
            CreateLeaveRequestInApprovalCmd createApprovalCmd = new CreateLeaveRequestInApprovalCmd();
            createApprovalCmd.setRequestingEmployeeId(requestingEmployeeId);
            createApprovalCmd.setRequestedLeaveTypeCode(cmd.getLeaveTypeCode());
            createApprovalCmd.setPlannedStartDate(cmd.getStartDate());
            createApprovalCmd.setPlannedEndDate(cmd.getEndDate());
        //    createApprovalCmd.setRequestedNumberOfDays(cmd.getNumberOfDays());
            createApprovalCmd.setReasonForLeaveRequest(cmd.getReason());
            createApprovalCmd.setInitialStatus("PENDING_VALIDATION");



            ResponseEntity<LeaveRequestStatusDto> approvalResponse = approvalServiceClient.createLeaveRequest(createApprovalCmd);
            if (approvalResponse.getStatusCode() != HttpStatus.CREATED || approvalResponse.getBody() == null) {
                throw new RuntimeException("Failed to create leave request in ApprovalService. Status: " + approvalResponse.getStatusCode());
            }
            String leaveRequestId = approvalResponse.getBody().getRequestId();
            saga.setCorrelationId(leaveRequestId); // Rất quan trọng -- thay thế trong hàm updateSagaStateInternal


            sagaPayload.setLeaveRequestId(leaveRequestId);
            updateSagaStateInternal(saga.getSagaId(), "CREATE_LEAVE_REQUEST_IN_APPROVAL", "COMPLETED_SUCCESS", "Request ID: " + leaveRequestId, sagaPayload);

            updateSagaStateInternal(saga.getSagaId(), "VALIDATE_LEAVE_REQUEST", "RUNNING", null, sagaPayload);
            EmployeeDetailsDto employeeDetails = getEmployeeDetails(requestingEmployeeId, saga.getSagaId()); // sagaId chỉ để log lỗi trong helper
            if (employeeDetails == null || employeeDetails.getDirectManagerId() == null) {
                String errorMsg = "Employee details or direct manager not found for employeeId: " + requestingEmployeeId;
                updateSagaStateInternal(saga.getSagaId(), "VALIDATE_LEAVE_REQUEST", "FAILED_NO_RETRY", errorMsg, sagaPayload);
                compensateCreateLeaveRequest(leaveRequestId, saga.getSagaId(), "NO_MANAGER_OR_EMP_DETAILS", sagaPayload);
                return saga.getSagaId();
            }
            sagaPayload.setEmployeeDetails(employeeDetails);
            LeaveValidationRequestDto validationRequest = new LeaveValidationRequestDto();

//            LeaveValidationRequestDto validationRequest = new LeaveValidationRequestDto(
//                    requestingEmployeeId,
//                    cmd.getLeaveTypeCode(),
//                    cmd.getStartDate().getYear(),
//                    cmd.getStartDate(),
//                    cmd.getEndDate(),
//                    cmd.getNumberOfDays() // Số ngày NV nhập
    //        );

            validationRequest.setEmployeeId(requestingEmployeeId);
            validationRequest.setLeaveTypeCode(cmd.getLeaveTypeCode());
            validationRequest.setForYear(cmd.getStartDate().getYear());
            validationRequest.setRequestedStartDate(cmd.getStartDate());
            validationRequest.setRequestedEndDate(cmd.getEndDate());
//            validationRequest.setRequestedDaysInputByEmployee(cmd.getNumberOfDays());




            ResponseEntity<LeaveValidationResponseDto> validationResponse = leaveServiceClient.validateLeaveRequest(validationRequest);


            if (validationResponse.getStatusCode() != HttpStatus.OK || validationResponse.getBody() == null) {
                throw new RuntimeException("LeaveService validation call failed. Status: " + validationResponse.getStatusCode());
            }
            LeaveValidationResponseDto validationResult = validationResponse.getBody();
            sagaPayload.setLeaveValidationResponse(validationResult);

            if (!validationResult.isValid()) {
                String reasons = String.join("; ", validationResult.getValidationMessages());
                updateSagaStateInternal(saga.getSagaId(), "VALIDATE_LEAVE_REQUEST", "FAILED_NO_RETRY", "Validation failed: " + reasons, sagaPayload);
                updateApprovalRequestStatus(leaveRequestId, "SYSTEM_REJECTED", reasons, saga.getSagaId(), sagaPayload, null); // Không có managerId cho system reject
                notifyEmployeeOfSystemRejection(employeeDetails, leaveRequestId, reasons, saga.getSagaId());
                return saga.getSagaId();
            }
            updateSagaStateInternal(saga.getSagaId(), "VALIDATE_LEAVE_REQUEST", "COMPLETED_SUCCESS", "Remaining: " + validationResult.getCalculatedRemainingDays(), sagaPayload);

            updateSagaStateInternal(saga.getSagaId(), "NOTIFY_MANAGER", "RUNNING", null, sagaPayload);
            updateApprovalRequestStatus(leaveRequestId, "PENDING_MANAGER_APPROVAL", "Awaiting manager decision.", saga.getSagaId(), sagaPayload, employeeDetails.getDirectManagerId());
            notifyManagerOfNewRequest(employeeDetails.getDirectManagerId(), employeeDetails, sagaPayload, saga.getSagaId());
            updateSagaStateInternal(saga.getSagaId(), "NOTIFY_MANAGER", "COMPLETED_SUCCESS", null, sagaPayload);

            updateSagaStateInternal(saga.getSagaId(), "AWAITING_MANAGER_DECISION", "COMPLETED_SUCCESS", null, sagaPayload);
            return saga.getSagaId();

        } catch (Exception e) {
            logger.error("Saga {} failed during initiation for employee {}: {}", saga.getSagaId(), requestingEmployeeId, e.getMessage(), e);
            // Lấy currentStep từ DB nếu saga đã được lưu, hoặc dùng step hiện tại nếu chưa
            String currentStepInError = saga.getCurrentSagaStepName() != null ? saga.getCurrentSagaStepName() : "SAGA_INITIATION_ERROR";
            updateSagaStateInternal(saga.getSagaId(), currentStepInError, "FAILED_NEEDS_COMPENSATION", e.getMessage(), sagaPayload);
            if (saga.getCorrelationId() != null && !"CREATE_LEAVE_REQUEST_IN_APPROVAL".equals(currentStepInError)) {
                compensateCreateLeaveRequest(saga.getCorrelationId(), saga.getSagaId(), "SAGA_INIT_FAILURE", sagaPayload);
            }
            return saga.getSagaId();
        }
    }

    @Transactional
    public void processManagerDecision(String leaveRequestId, ManagerDecisionCmd decisionCmd) {
        String sagaId = findSagaByCorrelationIdOrThrow(leaveRequestId, "AWAITING_MANAGER_DECISION");
        SagaInstance saga = sagaStateService.getSaga(sagaId); // Lấy saga instance đầy đủ
        if (saga == null) { // Kiểm tra lại cho chắc chắn
            logger.error("Saga instance not found for sagaId {} retrieved from correlationId {}", sagaId, leaveRequestId);
            throw new ResourceNotFoundException("Saga instance could not be retrieved for processing manager decision.");
        }
        LeaveRequestSagaPayload sagaPayload = parseSagaPayload(saga.getSagaPayloadData());

        updateSagaStateInternal(sagaId, "PROCESSING_MANAGER_DECISION", "RUNNING", "Manager ID: " + decisionCmd.getDecidingManagerId(), sagaPayload);

        String employeeId = sagaPayload.getCreateLeaveRequestCmd().getRequestingEmployeeId();
        EmployeeDetailsDto employeeDetails = sagaPayload.getEmployeeDetails();
        LeaveValidationResponseDto validationResult = sagaPayload.getLeaveValidationResponse();

        if (decisionCmd.getDecision() == DecisionType.APPROVED) {
            updateApprovalRequestStatus(leaveRequestId, "MANAGER_APPROVED", "Approved by manager.", sagaId, sagaPayload, decisionCmd.getDecidingManagerId());

            BigDecimal daysToDeduct = validationResult.getCalculatedBusinessDaysInRequest();
            if (daysToDeduct == null || daysToDeduct.compareTo(BigDecimal.ZERO) < 0) {
                logger.error("Saga {}: Invalid daysToDeduct ({}) from validationResult. Cannot proceed with balance update.", sagaId, daysToDeduct);
                updateSagaStateInternal(sagaId, "PROCESSING_MANAGER_DECISION", "FAILED_NO_RETRY", "Invalid daysToDeduct from validation.", sagaPayload);
                compensateApproveLeaveRequest(leaveRequestId, sagaId, "INVALID_DAYS_TO_DEDUCT_FROM_SAGA_PAYLOAD", sagaPayload);
                return;
            }

            UpdateBalanceAndLogHistoryCmd updateCmd = new UpdateBalanceAndLogHistoryCmd(
                    employeeId,
                    sagaPayload.getCreateLeaveRequestCmd().getLeaveTypeCode(),
                    sagaPayload.getCreateLeaveRequestCmd().getStartDate().getYear(),
                    daysToDeduct,
                    leaveRequestId,
                    "Approved by manager: " + decisionCmd.getDecidingManagerId(),
                    sagaPayload.getCreateLeaveRequestCmd().getStartDate(),
                    sagaPayload.getCreateLeaveRequestCmd().getEndDate()
            );
            updateEmployeeLeaveData(updateCmd, sagaId, sagaPayload);
            notifyEmployeeOfApproval(employeeDetails, leaveRequestId, saga.getSagaId());
            updateSagaStateInternal(sagaId, "PROCESSING_MANAGER_DECISION", "COMPLETED_APPROVED", null, sagaPayload);

        } else { // REJECTED
            updateApprovalRequestStatus(leaveRequestId, "MANAGER_REJECTED", decisionCmd.getRejectionReason(), sagaId, sagaPayload, decisionCmd.getDecidingManagerId());
            notifyEmployeeOfRejection(employeeDetails, leaveRequestId, decisionCmd.getRejectionReason(), saga.getSagaId());
            updateSagaStateInternal(sagaId, "PROCESSING_MANAGER_DECISION", "COMPLETED_REJECTED", null, sagaPayload);
        }
        updateSagaStateInternal(sagaId, "FINALIZED", "COMPLETED", "Saga finished.", sagaPayload);
    }

    private EmployeeDetailsDto getEmployeeDetails(String employeeId, String sagaIdForLogging) {
        try {
            ResponseEntity<EmployeeDetailsDto> response = employeeServiceClient.getEmployeeDetails(employeeId);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            logger.warn("Saga {}: Failed to get employee details from EmployeeService for employeeId {}. Status: {}", sagaIdForLogging, employeeId, response.getStatusCode());
            // Không ném lỗi ở đây ngay, để saga có thể ghi nhận FAILED_NO_RETRY nếu logic chính quyết định
            return null; // Hoặc throw exception cụ thể hơn nếu đây là lỗi nghiêm trọng không thể tiếp tục
        } catch (Exception e) {
            logger.error("Saga {}: Error calling EmployeeService for details of employeeId {}: {}", sagaIdForLogging, employeeId, e.getMessage(), e);
            // Không gọi updateSagaState ở đây, để hàm gọi chính xử lý
            throw new RuntimeException("Communication error with EmployeeService while fetching employee details.", e);
        }
    }

    private void updateApprovalRequestStatus(String leaveRequestId, String status, String notes, String sagaId, LeaveRequestSagaPayload sagaPayload, String approverId) {
        try {
            UpdateLeaveRequestStatusCmd cmd = new UpdateLeaveRequestStatusCmd();
            cmd.setNewStatus(status);
            cmd.setNotes(notes);
            if (approverId != null) { // Chỉ gán nếu approverId được cung cấp
                cmd.setAssignedApproverId(approverId);
            }
            if ("MANAGER_APPROVED".equals(status) || "MANAGER_REJECTED".equals(status)) {
                cmd.setManagerDecision(status.replace("MANAGER_", ""));
                if ("MANAGER_REJECTED".equals(status)) {
                    cmd.setManagerRejectionReason(notes); // notes chính là lý do từ chối
                }
            }
            approvalServiceClient.updateLeaveRequestStatus(leaveRequestId, cmd);
        } catch (Exception e) {
            updateSagaStateInternal(sagaId, "UPDATE_APPROVAL_STATUS", "FAILED_NEEDS_COMPENSATION", e.getMessage(), sagaPayload);
            logger.error("Saga {}: Error calling ApprovalService to update status for requestId {}: {}", sagaId, leaveRequestId, e.getMessage(), e);
            throw e;
        }
    }

    private void notifyEmployeeOfSystemRejection(EmployeeDetailsDto employeeDetails, String leaveRequestId, String reason, String sagaId) {
        SendNotificationCmd notificationCmd = new SendNotificationCmd();
        notificationCmd.setRecipientIdentifier(employeeDetails.getEmailAddress()); // Sử dụng email từ DTO
        notificationCmd.setTemplateCode("LEAVE_REQUEST_SYSTEM_REJECTED_EMP"); // Khớp với template đã tạo
        notificationCmd.setTemplateParameters(Map.of("requestId", leaveRequestId, "reason", reason, "employeeName", employeeDetails.getFullName()));
        notificationCmd.setRelatedLeaveRequestId(leaveRequestId);
        notificationCmd.setRelatedSagaId(sagaId);
        safeCallNotificationService(notificationCmd, sagaId, "NOTIFY_EMPLOYEE_SYSTEM_REJECTION");
    }

    private void notifyManagerOfNewRequest(String managerId, EmployeeDetailsDto requestingEmployeeDetails, LeaveRequestSagaPayload sagaPayload, String sagaId) {
        EmployeeDetailsDto managerDetails = getEmployeeDetails(managerId, sagaId); // Lấy thông tin quản lý (bao gồm email)
        if (managerDetails == null || managerDetails.getEmailAddress() == null) {
            logger.error("Saga {}: Cannot notify manager. Manager details or email not found for managerId {}", sagaId, managerId);
            // Có thể cần đánh dấu bước này thất bại trong Saga nếu thông báo cho quản lý là bắt buộc
            updateSagaStateInternal(sagaId, "NOTIFY_MANAGER", "FAILED_NO_RETRY", "Manager email not found", sagaPayload);
            // Kích hoạt bù trừ nếu cần
            compensateCreateLeaveRequest(sagaPayload.getLeaveRequestId(), sagaId, "MANAGER_EMAIL_NOT_FOUND_FOR_NOTIFICATION", sagaPayload);
            throw new RuntimeException("Manager email not found for notification in saga " + sagaId);
        }

        SendNotificationCmd notificationCmd = new SendNotificationCmd();
        notificationCmd.setRecipientIdentifier(managerDetails.getEmailAddress().trim());
        notificationCmd.setTemplateCode("LEAVE_REQUEST_SUBMITTED_TO_MANAGER");
        notificationCmd.setTemplateParameters(Map.of(

                "requestId", Objects.toString(sagaPayload.getLeaveRequestId(), "N/A"),
                "employeeName", Objects.toString(requestingEmployeeDetails.getFullName(), "N/A"),
                "leaveTypeName", Objects.toString(sagaPayload.getCreateLeaveRequestCmd().getLeaveTypeCode(), "N/A"),
                "startDate", Objects.toString(sagaPayload.getCreateLeaveRequestCmd().getStartDate(), "N/A"),
                "endDate", Objects.toString(sagaPayload.getCreateLeaveRequestCmd().getEndDate(), "N/A"),
                //"numberOfDays", Objects.toString(sagaPayload.getCreateLeaveRequestCmd().getNumberOfDays(), "N/A"),
                "reason", Objects.toString(sagaPayload.getCreateLeaveRequestCmd().getReason(), "")
        ));
        notificationCmd.setChannel("EMAIL");
        notificationCmd.setRelatedLeaveRequestId(sagaPayload.getLeaveRequestId());
        notificationCmd.setRelatedSagaId(sagaId);
        safeCallNotificationService(notificationCmd, sagaId, "NOTIFY_MANAGER_OF_NEW_REQUEST");
    }

    private void updateEmployeeLeaveData(UpdateBalanceAndLogHistoryCmd cmd, String sagaId, LeaveRequestSagaPayload sagaPayload) {
        try {
            ResponseEntity<Void> response = employeeServiceClient.updateBalanceAndLogHistory(cmd);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("EmployeeService failed to update balance/history. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            updateSagaStateInternal(sagaId, "UPDATE_EMPLOYEE_LEAVE_DATA", "FAILED_NEEDS_COMPENSATION", e.getMessage(), sagaPayload);
            logger.error("Saga {}: Error calling EmployeeService to update balance/history for request {}: {}", sagaId, cmd.getOriginalLeaveRequestId(), e.getMessage(), e);
            compensateApproveLeaveRequest(cmd.getOriginalLeaveRequestId(), sagaId, "FAILED_TO_UPDATE_EMPLOYEE_BALANCE", sagaPayload);
            throw e;
        }
    }

    private void notifyEmployeeOfApproval(EmployeeDetailsDto employeeDetails, String leaveRequestId, String sagaId) {
        SendNotificationCmd cmd = new SendNotificationCmd();
        cmd.setRecipientIdentifier(employeeDetails.getEmailAddress());
        cmd.setTemplateCode("LEAVE_REQUEST_APPROVED_TO_EMPLOYEE");
        // Lấy thông tin từ sagaPayload nếu cần chi tiết hơn cho template
        LeaveRequestSagaPayload payload = parseSagaPayload(sagaStateService.getSaga(sagaId).getSagaPayloadData());
        cmd.setTemplateParameters(Map.of(
                "requestId", leaveRequestId,
                "employeeName", employeeDetails.getFullName(),
                "leaveTypeName", payload.getCreateLeaveRequestCmd().getLeaveTypeCode(),
                "startDate", payload.getCreateLeaveRequestCmd().getStartDate().toString(),
                "endDate", payload.getCreateLeaveRequestCmd().getEndDate().toString(),
                "numberOfDays", payload.getLeaveValidationResponse().getCalculatedBusinessDaysInRequest()
        ));
        cmd.setRelatedLeaveRequestId(leaveRequestId);
        cmd.setRelatedSagaId(sagaId);
        safeCallNotificationService(cmd, sagaId, "NOTIFY_EMPLOYEE_APPROVED");
    }

    private void notifyEmployeeOfRejection(EmployeeDetailsDto employeeDetails, String leaveRequestId, String reason, String sagaId) {
        SendNotificationCmd cmd = new SendNotificationCmd();
        cmd.setRecipientIdentifier(employeeDetails.getEmailAddress());
        cmd.setTemplateCode("LEAVE_REQUEST_REJECTED_TO_EMPLOYEE");
        LeaveRequestSagaPayload payload = parseSagaPayload(sagaStateService.getSaga(sagaId).getSagaPayloadData());
        cmd.setTemplateParameters(Map.of(
                "requestId", leaveRequestId,
                "employeeName", employeeDetails.getFullName(),
                "leaveTypeName", payload.getCreateLeaveRequestCmd().getLeaveTypeCode(),
                "rejectionReason", reason
        ));
        cmd.setRelatedLeaveRequestId(leaveRequestId);
        cmd.setRelatedSagaId(sagaId);
        safeCallNotificationService(cmd, sagaId, "NOTIFY_EMPLOYEE_REJECTED");
    }

    private void safeCallNotificationService(SendNotificationCmd cmd, String sagaId, String stepNameForLog) {
        try {
            logger.info("Saga {}: Attempting to send notification for step '{}', recipient: {}, template: {}",
                    sagaId, stepNameForLog, cmd.getRecipientIdentifier(), cmd.getTemplateCode());
            notificationServiceClient.sendNotification(cmd);
            logger.info("Saga {}: Notification command sent for step '{}'", sagaId, stepNameForLog);
        } catch (Exception e) {
            logger.error("Saga {}: Failed to send notification for step '{}' (non-critical, proceeding saga): {}",
                    sagaId, stepNameForLog, e.getMessage(), e);
            // Gửi thông báo thường không nên làm fail Saga, chỉ log lỗi.
            // Nếu thông báo là critical, bạn cần thay đổi logic ở đây để updateSagaState thành FAILED_NEEDS_COMPENSATION
        }
    }

    // --- Compensation methods ---
    private void compensateCreateLeaveRequest(String leaveRequestId, String sagaId, String reason, LeaveRequestSagaPayload sagaPayload) {
        logger.warn("Saga {}: Compensating CreateLeaveRequest for requestId {}. Reason: {}", sagaId, leaveRequestId, reason);
        updateSagaStateInternal(sagaId, "COMPENSATE_CREATE_LEAVE_REQUEST", "RUNNING", reason, sagaPayload);
        try {
            updateApprovalRequestStatus(leaveRequestId, "CANCELLED_BY_SAGA", "Saga compensation: " + reason, sagaId, sagaPayload, null);
            updateSagaStateInternal(sagaId, "COMPENSATE_CREATE_LEAVE_REQUEST", "COMPLETED_SUCCESS", null, sagaPayload);
        } catch (Exception e) {
            logger.error("Saga {}: Failed to compensate CreateLeaveRequest for requestId {}: {}", sagaId, leaveRequestId, e.getMessage(), e);
            updateSagaStateInternal(sagaId, "COMPENSATE_CREATE_LEAVE_REQUEST", "FAILED_NO_RETRY", e.getMessage(), sagaPayload);
        }
    }

    private void compensateApproveLeaveRequest(String leaveRequestId, String sagaId, String reason, LeaveRequestSagaPayload sagaPayload) {
        logger.warn("Saga {}: Compensating ApproveLeaveRequest for requestId {}. Reason: {}", sagaId, leaveRequestId, reason);
        updateSagaStateInternal(sagaId, "COMPENSATE_APPROVE_LEAVE", "RUNNING", reason, sagaPayload);
        try {
            // 1. Yêu cầu ApprovalService chuyển trạng thái về lại, ví dụ PENDING_MANAGER_APPROVAL hoặc một trạng thái lỗi/bù trừ
            updateApprovalRequestStatus(leaveRequestId, "APPROVAL_COMPENSATION_NEEDED", "Saga compensation: " + reason, sagaId, sagaPayload, null);

            // 2. Hoàn lại số ngày nghỉ đã trừ cho nhân viên (quan trọng!)
            // Cần lấy thông tin từ sagaPayload để tạo lệnh hoàn trả chính xác
            BigDecimal daysDeducted = sagaPayload.getLeaveValidationResponse().getCalculatedBusinessDaysInRequest();
            CreateLeaveRequestCmd originalCmd = sagaPayload.getCreateLeaveRequestCmd();

            if (daysDeducted != null && daysDeducted.compareTo(BigDecimal.ZERO) > 0) {
                UpdateBalanceAndLogHistoryCmd revertCmd = new UpdateBalanceAndLogHistoryCmd(
                        originalCmd.getRequestingEmployeeId(),
                        originalCmd.getLeaveTypeCode(),
                        originalCmd.getStartDate().getYear(),
                        daysDeducted.negate(), // Số ngày hoàn lại (âm của số ngày đã trừ)
                        leaveRequestId,
                        "Leave approval compensated: " + reason,
                        originalCmd.getStartDate(),
                        originalCmd.getEndDate()
                );
                // API updateBalanceAndLogHistory của EmployeeService cần hỗ trợ việc cộng lại ngày (nhận số âm)
                // Hoặc tạo một API riêng cho việc hoàn trả.
                // Hiện tại, giả sử nó xử lý được số âm.
                employeeServiceClient.updateBalanceAndLogHistory(revertCmd);
                logger.info("Saga {}: Sent command to EmployeeService to revert leave balance for request {}", sagaId, leaveRequestId);
            } else {
                logger.warn("Saga {}: No days to revert or daysDeducted is null in payload for request {}", sagaId, leaveRequestId);
            }

            updateSagaStateInternal(sagaId, "COMPENSATE_APPROVE_LEAVE", "COMPLETED_SUCCESS", "Leave approval compensated. Balance restoration initiated.", sagaPayload);
        } catch (Exception e) {
            logger.error("Saga {}: Failed to fully compensate ApproveLeaveRequest for requestId {}: {}", sagaId, leaveRequestId, e.getMessage(), e);
            updateSagaStateInternal(sagaId, "COMPENSATE_APPROVE_LEAVE", "FAILED_NO_RETRY", "Compensation failed: " + e.getMessage(), sagaPayload);
        }
    }

    private String findSagaByCorrelationIdOrThrow(String correlationId, String expectedStatus) {
        List<SagaInstance> sagas = sagaInstanceRepository.findByCorrelationIdAndCurrentSagaStepName(correlationId, expectedStatus);
        if (sagas.isEmpty()) {
            sagas = sagaInstanceRepository.findByCorrelationIdAndCurrentSagaStepName(correlationId, "RUNNING");
            if (sagas.isEmpty()) {
                throw new ResourceNotFoundException("Active Saga not found or not in expected state for correlationId: " + correlationId + " (expected: " + expectedStatus + " or RUNNING)");
            }
        }
        if (sagas.size() > 1) {
            logger.warn("Multiple sagas found for correlationId: {} and status {}. Using the latest created.", correlationId, expectedStatus);
            sagas.sort((s1, s2) -> s2.getSagaCreatedAt().compareTo(s1.getSagaCreatedAt()));
        }
        return sagas.get(0).getSagaId();
    }

    private LeaveRequestSagaPayload parseSagaPayload(String payloadData) {
        try {
            if (payloadData == null || payloadData.isEmpty() || "null".equalsIgnoreCase(payloadData)) {
                logger.warn("Saga payload data is null or empty, returning new payload object.");
                return new LeaveRequestSagaPayload();
            }
            return objectMapper.readValue(payloadData, LeaveRequestSagaPayload.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse saga payload data: '{}'. Error: {}", payloadData, e.getMessage());
            throw new RuntimeException("Error parsing saga payload.", e);
        }
    }

    private BigDecimal getBusinessDaysFromSagaPayload(SagaInstance saga) {
        LeaveRequestSagaPayload payload = parseSagaPayload(saga.getSagaPayloadData());
        if (payload.getLeaveValidationResponse() != null && payload.getLeaveValidationResponse().getCalculatedBusinessDaysInRequest() != null) {
            return payload.getLeaveValidationResponse().getCalculatedBusinessDaysInRequest();
        }
        logger.warn("Saga {}: Calculated business days not found in saga payload. Falling back to originally requested days. THIS IS LIKELY AN ERROR IF LEAVESERVICE WAS EXPECTED TO CALCULATE BUSINESS DAYS.", saga.getSagaId());
        // Fallback nguy hiểm, chỉ nên xảy ra nếu có lỗi logic nghiêm trọng
        if (payload.getCreateLeaveRequestCmd() != null) {
            return payload.getLeaveValidationResponse().getCalculatedBusinessDaysInRequest();
        }
        throw new IllegalStateException("Cannot determine days to deduct for saga " + saga.getSagaId());
    }
}