package com.example.managerservice.service;


import com.example.managerservice.client.ApprovalServiceClient;
import com.example.managerservice.client.EmployeeServiceClient;
import com.example.managerservice.dto.ManagedEmployeeDto;
import com.example.managerservice.dto.PendingApprovalRequestDto;
import com.example.managerservice.dto.ext.EmployeeDetailsDto;
import com.example.managerservice.dto.ext.LeaveRequestStatusDto;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ManagerDashboardService {
    private static final Logger logger = LoggerFactory.getLogger(ManagerDashboardService.class);

    private final EmployeeServiceClient employeeServiceClient;
    private final ApprovalServiceClient approvalServiceClient;

    public ManagerDashboardService(EmployeeServiceClient employeeServiceClient,
                                   ApprovalServiceClient approvalServiceClient) {
        this.employeeServiceClient = employeeServiceClient;
        this.approvalServiceClient = approvalServiceClient;
    }

    public List<ManagedEmployeeDto> getManagedEmployees(String managerId) {
        try {
            ResponseEntity<List<EmployeeDetailsDto>> response = employeeServiceClient.getEmployeesByManagerId(managerId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().stream()
                        .map(this::convertToManagedEmployeeDto)
                        .collect(Collectors.toList());
            }
            logger.warn("Could not retrieve managed employees for managerId {}: Status {}", managerId, response.getStatusCode());
        } catch (FeignException e) {
            logger.error("Error calling EmployeeService for managed employees (managerId {}): {}", managerId, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<PendingApprovalRequestDto> getPendingLeaveRequestsForManager(String managerId) {
        try {
            ResponseEntity<List<LeaveRequestStatusDto>> response = approvalServiceClient.getPendingApprovalsForManager(managerId, "PENDING_MANAGER_APPROVAL");
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().stream()
                        .map(this::convertToPendingApprovalDto)
                        .filter(Objects::nonNull) // Bỏ qua nếu không lấy được thông tin NV
                        .collect(Collectors.toList());
            }
            logger.warn("Could not retrieve pending leave requests for managerId {}: Status {}", managerId, response.getStatusCode());
        } catch (FeignException e) {
            logger.error("Error calling ApprovalService for pending requests (managerId {}): {}", managerId, e.getMessage());
        }
        return Collections.emptyList();
    }

    private ManagedEmployeeDto convertToManagedEmployeeDto(EmployeeDetailsDto empDetails) {
        if (empDetails == null) return null;
        ManagedEmployeeDto dto = new ManagedEmployeeDto();
        dto.setEmployeeId(empDetails.getEmployeeId());
        dto.setEmployeeCode(empDetails.getEmployeeCode());
        dto.setFullName(empDetails.getFullName());
        dto.setEmailAddress(empDetails.getEmailAddress());
        dto.setJobTitle(empDetails.getJobTitle());
        dto.setDepartmentName(empDetails.getDepartmentName()); // Giả sử EmployeeDetailsDto có trường này
        return dto;
    }

    private PendingApprovalRequestDto convertToPendingApprovalDto(LeaveRequestStatusDto requestStatus) {
        if (requestStatus == null) return null;

        // Lấy thêm thông tin nhân viên yêu cầu và loại nghỉ từ EmployeeService
        EmployeeDetailsDto requestingEmployee = null;
        try {
            ResponseEntity<EmployeeDetailsDto> empResponse = employeeServiceClient.getEmployeeDetails(requestStatus.getRequestingEmployeeId());
            if (empResponse.getStatusCode().is2xxSuccessful()) {
                requestingEmployee = empResponse.getBody();
            }
        } catch (FeignException e) {
            logger.error("Error fetching employee details for {} during DTO conversion: {}", requestStatus.getRequestingEmployeeId(), e.getMessage());
            // Có thể bỏ qua yêu cầu này hoặc trả về DTO với thông tin bị thiếu
            return null;
        }

        PendingApprovalRequestDto dto = new PendingApprovalRequestDto();
        dto.setRequestId(requestStatus.getRequestId());
        dto.setRequestingEmployeeId(requestStatus.getRequestingEmployeeId());
        if (requestingEmployee != null) {
            dto.setRequestingEmployeeName(requestingEmployee.getFullName());
            // Giả sử EmployeeDetailsDto có thể trả về thông tin về loại nghỉ hoặc bạn cần gọi một endpoint khác
            // Hoặc, thông tin loại nghỉ đã có sẵn trong LeaveRequestStatusDto từ ApprovalService
            // dto.setLeaveTypeName( Lấy từ EmployeeService hoặc từ requestStatus.getRequestedLeaveTypeName() nếu có );
        }
        dto.setLeaveTypeName(requestStatus.getRequestedLeaveTypeCode()); // Tạm dùng code, cần join để có tên
        dto.setPlannedStartDate(requestStatus.getPlannedStartDate());
        dto.setPlannedEndDate(requestStatus.getPlannedEndDate());
        dto.setRequestedNumberOfDays(requestStatus.getRequestedNumberOfDays());
        dto.setReasonForLeaveRequest(requestStatus.getReasonForLeaveRequest());
        dto.setRequestSubmissionTimestamp(requestStatus.getRequestSubmissionTimestamp());
        return dto;
    }
}