package com.example.leaverequestservice.service;

import com.example.leaverequestservice.entity.SagaInstance;
import com.example.leaverequestservice.entity.SagaStepLog;
import com.example.leaverequestservice.repository.SagaInstanceRepository;
import com.example.leaverequestservice.repository.SagaStepLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson cho JSON
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SagaStateService {
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepLogRepository sagaStepLogRepository;
    private final ObjectMapper objectMapper; // Để chuyển đổi payload sang JSON String

    public SagaStateService(SagaInstanceRepository sagaInstanceRepository,
                            SagaStepLogRepository sagaStepLogRepository,
                            ObjectMapper objectMapper) {
        this.sagaInstanceRepository = sagaInstanceRepository;
        this.sagaStepLogRepository = sagaStepLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SagaInstance createSaga(String sagaType, String correlationId, String payloadData) { // Nhận String
        SagaInstance saga = new SagaInstance();
        saga.setSagaId(UUID.randomUUID().toString());
        saga.setSagaTypeName(sagaType);
        saga.setCorrelationId(correlationId);
        saga.setSagaPayloadData(payloadData); // Gán trực tiếp
        saga.setSagaStatus("RUNNING");
        saga.setCurrentSagaStepName("INITIALIZED");
        return sagaInstanceRepository.save(saga);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSagaState(String sagaId, String currentStep, String status, String errorDetails, String payloadData) { // Nhận String
        SagaInstance saga = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));
        saga.setCurrentSagaStepName(currentStep);
        saga.setSagaStatus(status);
        if (payloadData != null) { // Chỉ cập nhật payload nếu được cung cấp
            saga.setSagaPayloadData(payloadData);
        }
        // ... (phần còn lại của hàm) ...
        sagaInstanceRepository.save(saga);
        logStep(saga, currentStep, status, errorDetails);
    }

    private void logStep(SagaInstance saga, String stepName, String status, String details) {
        SagaStepLog stepLog = new SagaStepLog();
        stepLog.setSagaInstance(saga);
        stepLog.setStepName(stepName);
        stepLog.setStepStatus(status);
        stepLog.setStepErrorOrInfoDetails(details);
        sagaStepLogRepository.save(stepLog);
    }

    public SagaInstance getSaga(String sagaId){
        return sagaInstanceRepository.findById(sagaId).orElse(null);
    }
}