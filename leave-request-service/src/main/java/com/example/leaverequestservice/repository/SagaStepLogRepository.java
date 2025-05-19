package com.example.leaverequestservice.repository;

import com.example.leaverequestservice.entity.SagaStepLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaStepLogRepository extends JpaRepository<SagaStepLog, String> {
}
