package com.example.leaveservice.controller;

import com.example.leaveservice.dto.LeaveValidationRequest;
import com.example.leaveservice.dto.LeaveValidationResponse;
import com.example.leaveservice.service.LeaveValidationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave-validation") // Đổi tên API cho rõ ràng hơn
public class LeaveValidationController {

    private final LeaveValidationService validationService;

    public LeaveValidationController(LeaveValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<LeaveValidationResponse> validateLeaveRequest(
            @Valid @RequestBody LeaveValidationRequest request) {
        LeaveValidationResponse response = validationService.validateLeaveRequest(request);
        return ResponseEntity.ok(response);
    }
}