package com.example.employeeservice.service;

import com.example.employeeservice.dto.EmployeeLeaveHistoryDto;
import com.example.employeeservice.entity.Employee;
import com.example.employeeservice.entity.EmployeeLeaveHistory;
import com.example.employeeservice.exception.ResourceNotFoundException;
import com.example.employeeservice.repository.EmployeeLeaveHistoryRepository;
import com.example.employeeservice.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeLeaveHistoryManagementService {
    private final EmployeeLeaveHistoryRepository historyRepository;
    private final EmployeeRepository employeeRepository;
    // private final LeaveHistoryMapper leaveHistoryMapper;


    public EmployeeLeaveHistoryManagementService(EmployeeLeaveHistoryRepository historyRepository,
                                                 EmployeeRepository employeeRepository
            /*, LeaveHistoryMapper leaveHistoryMapper*/) {
        this.historyRepository = historyRepository;
        this.employeeRepository = employeeRepository;
        // this.leaveHistoryMapper = leaveHistoryMapper;
    }

    @Transactional(readOnly = true)
    public List<EmployeeLeaveHistoryDto> getLeaveHistoryForEmployee(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return historyRepository.findByEmployeeOrderByActualLeaveStartDateDesc(employee)
                .stream()
                .map(this::convertToLeaveHistoryDto) // leaveHistoryMapper::toDto
                .collect(Collectors.toList());
    }

    // Hàm mapper thủ công ví dụ
    private EmployeeLeaveHistoryDto convertToLeaveHistoryDto(EmployeeLeaveHistory entity) {
        if (entity == null) return null;
        EmployeeLeaveHistoryDto dto = new EmployeeLeaveHistoryDto();
        dto.setHistoryId(entity.getHistoryId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setOriginalLeaveRequestId(entity.getOriginalLeaveRequestId());
        dto.setLeaveTypeCode(entity.getLeaveType().getLeaveTypeCode());
        dto.setLeaveTypeName(entity.getLeaveType().getLeaveTypeName()); // Lấy thêm tên loại nghỉ
        dto.setActualLeaveStartDate(entity.getActualLeaveStartDate());
        dto.setActualLeaveEndDate(entity.getActualLeaveEndDate());
        dto.setActualDaysOfLeave(entity.getActualDaysOfLeave());
        dto.setReasonForApprovedLeave(entity.getReasonForApprovedLeave());
        dto.setRequestStatusWhenLogged(entity.getRequestStatusWhenLogged());
        dto.setHistoryLoggedAt(entity.getHistoryLoggedAt());
        return dto;
    }
}
