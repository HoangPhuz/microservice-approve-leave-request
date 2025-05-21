package com.example.leaverequestservice.repository;

import com.example.leaverequestservice.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, String> {
    List<SagaInstance> findByCorrelationIdAndCurrentSagaStepName(String correlationId, String expectedStatus);
}
