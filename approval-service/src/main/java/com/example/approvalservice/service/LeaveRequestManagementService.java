package com.example.approvalservice.service;



import com.example.approvalservice.dto.CreateLeaveRequestCmd;
import com.example.approvalservice.dto.LeaveRequestDto;
import com.example.approvalservice.dto.UpdateLeaveRequestStatusCmd;
import com.example.approvalservice.entity.LeaveRequest;
import com.example.approvalservice.exception.ResourceNotFoundException;
import com.example.approvalservice.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveRequestManagementService {

    private final LeaveRequestRepository leaveRequestRepository;


    public LeaveRequestManagementService(LeaveRequestRepository leaveRequestRepository ) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @Transactional
    public LeaveRequestDto createLeaveRequest(CreateLeaveRequestCmd cmd) {
        LeaveRequest leaveRequest = new LeaveRequest();
        // Ánh xạ từ cmd sang entity (có thể dùng mapper)
        leaveRequest.setRequestingEmployeeId(cmd.getRequestingEmployeeId());
        leaveRequest.setRequestedLeaveTypeCode(cmd.getRequestedLeaveTypeCode());
        leaveRequest.setPlannedStartDate(cmd.getPlannedStartDate());
        leaveRequest.setPlannedEndDate(cmd.getPlannedEndDate());
        //leaveRequest.setRequestedNumberOfDays(cmd.getRequestedNumberOfDays());
        leaveRequest.setReasonForLeaveRequest(cmd.getReasonForLeaveRequest());
        leaveRequest.setCurrentRequestStatus(cmd.getInitialStatus());
        leaveRequest.setOrchestratingSagaId(cmd.getOrchestratingSagaId());

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        return convertToDto(savedRequest);
    }

    @Transactional
    public LeaveRequestDto updateLeaveRequest(String requestId, UpdateLeaveRequestStatusCmd cmd) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu nghỉ phép với id: " + requestId));
        leaveRequest.setCurrentRequestStatus(cmd.getNewStatus());

        if (cmd.getAssignedApproverId() != null) {
            leaveRequest.setAssignedApproverId(cmd.getAssignedApproverId());
        }
        if ("MANAGER_APPROVED".equals(cmd.getNewStatus()) || "MANAGER_REJECTED".equals(cmd.getNewStatus())) {
            leaveRequest.setManagerDecision(cmd.getManagerDecision());
            leaveRequest.setManagerDecisionTimestamp(cmd.getManagerDecisionTimestamp() != null ? cmd.getManagerDecisionTimestamp() : LocalDateTime.now());
            if ("MANAGER_REJECTED".equals(cmd.getNewStatus())) {
                leaveRequest.setManagerRejectionReason(cmd.getNotesOrReason()); // Hoặc cmd.getManagerRejectionReason()
            } else {
                leaveRequest.setManagerReviewNotes(cmd.getNotesOrReason()); // Ghi chú khi duyệt
            }
        } else {
            leaveRequest.setManagerReviewNotes(cmd.getNotesOrReason()); // Ghi chú cho các trạng thái khác
        }


        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        return convertToDto(updatedRequest); // leaveRequestMapper.toDto(updatedRequest);
    }

    @Transactional(readOnly = true)
    public LeaveRequestDto getLeaveRequestById(String requestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest not found with id: " + requestId));
        return convertToDto(leaveRequest);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getLeaveRequestsByApproverAndStatus(String approverId, String status) {
        return leaveRequestRepository.findByAssignedApproverIdAndCurrentRequestStatus(approverId, status)
                .stream()
                .map(this::convertToDto) // .map(leaveRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getLeaveRequestsByRequestingEmployee(String employeeId) {
        return leaveRequestRepository.findByRequestingEmployeeIdOrderByRequestSubmissionTimestampDesc(employeeId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // Hàm mapper
    private LeaveRequestDto convertToDto(LeaveRequest entity) {
        if (entity == null) return null;
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setRequestId(entity.getRequestId());
        dto.setRequestingEmployeeId(entity.getRequestingEmployeeId());
        dto.setRequestedLeaveTypeCode(entity.getRequestedLeaveTypeCode());
        dto.setAssignedApproverId(entity.getAssignedApproverId());
        dto.setOrchestratingSagaId(entity.getOrchestratingSagaId());
        dto.setPlannedStartDate(entity.getPlannedStartDate());
        dto.setPlannedEndDate(entity.getPlannedEndDate());
        //dto.setRequestedNumberOfDays(entity.getRequestedNumberOfDays());
        dto.setReasonForLeaveRequest(entity.getReasonForLeaveRequest());
        dto.setRequestSubmissionTimestamp(entity.getRequestSubmissionTimestamp());
        dto.setCurrentRequestStatus(entity.getCurrentRequestStatus());
        dto.setManagerDecisionTimestamp(entity.getManagerDecisionTimestamp());
        dto.setManagerDecision(entity.getManagerDecision());
        dto.setManagerRejectionReason(entity.getManagerRejectionReason());
        dto.setManagerReviewNotes(entity.getManagerReviewNotes());
        dto.setRequestCreatedAt(entity.getRequestCreatedAt());
        dto.setRequestUpdatedAt(entity.getRequestUpdatedAt());
        return dto;
    }
}