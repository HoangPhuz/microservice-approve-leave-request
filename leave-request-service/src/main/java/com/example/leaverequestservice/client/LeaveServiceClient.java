
package com.example.leaverequestservice.client;

import com.example.leaverequestservice.dto.ext.LeaveValidationRequestDto; // Input cho LeaveService
import com.example.leaverequestservice.dto.ext.LeaveValidationResponseDto; // Output từ LeaveService
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "leave-service", path = "/api/leave-validation")
public interface LeaveServiceClient {

    @PostMapping("/validate")
    ResponseEntity<LeaveValidationResponseDto> validateLeaveRequest(@RequestBody LeaveValidationRequestDto request);
}