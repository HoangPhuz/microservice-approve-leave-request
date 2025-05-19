package com.example.employeeservice.service;

import com.example.employeeservice.dto.EmployeeLeaveBalanceDto;
import com.example.employeeservice.dto.UpdateBalanceAndLogHistoryRequest;
import com.example.employeeservice.entity.Employee;
import com.example.employeeservice.entity.EmployeeLeaveBalance;
import com.example.employeeservice.entity.EmployeeLeaveHistory;
import com.example.employeeservice.entity.LeaveType;
import com.example.employeeservice.exception.ResourceNotFoundException;
import com.example.employeeservice.repository.EmployeeLeaveBalanceRepository;
import com.example.employeeservice.repository.EmployeeLeaveHistoryRepository;
import com.example.employeeservice.repository.EmployeeRepository;
import com.example.employeeservice.repository.LeaveTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeLeaveBalanceManagementService {
    private final EmployeeLeaveBalanceRepository balanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeLeaveHistoryRepository historyRepository;
    // private final LeaveBalanceMapper leaveBalanceMapper;

    public EmployeeLeaveBalanceManagementService(EmployeeLeaveBalanceRepository balanceRepository,
                                                 EmployeeRepository employeeRepository,
                                                 LeaveTypeRepository leaveTypeRepository,
                                                 EmployeeLeaveHistoryRepository historyRepository
            /*, LeaveBalanceMapper leaveBalanceMapper*/) {
        this.balanceRepository = balanceRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.historyRepository = historyRepository;
        // this.leaveBalanceMapper = leaveBalanceMapper;
    }

    @Transactional(readOnly = true)
    public List<EmployeeLeaveBalanceDto> getLeaveBalancesForEmployee(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return balanceRepository.findByEmployee(employee)
                .stream()
                .map(this::convertToLeaveBalanceDto) // leaveBalanceMapper::toDto
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeLeaveBalanceDto getSpecificLeaveBalance(String employeeId, String leaveTypeCode, int year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType not found: " + leaveTypeCode));

        EmployeeLeaveBalance balance = balanceRepository
                .findByEmployeeAndLeaveTypeAndBalanceApplicableYear(employee, leaveType, year)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave balance not found for employee " + employeeId +
                                ", type " + leaveTypeCode + ", year " + year));
//        balance.calculateRemainingDays(); // Gọi hàm tính toán lại nếu cần thiết
        return convertToLeaveBalanceDto(balance); // leaveBalanceMapper.toDto(balance);
    }

    @Transactional
    public void updateBalanceAndLogLeave(UpdateBalanceAndLogHistoryRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.getEmployeeId()));
        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType not found: " + request.getLeaveTypeCode()));

        EmployeeLeaveBalance balance = balanceRepository
                .findByEmployeeAndLeaveTypeAndBalanceApplicableYear(employee, leaveType, request.getApplicableYear())
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found for the specified year and type."));

//        BigDecimal currentDaysTaken = balance.getDaysTaken() != null ? balance.getDaysTaken() : BigDecimal.ZERO;
//        BigDecimal newDaysTaken = currentDaysTaken.add(request.getDaysToDeduct());

        // Kiểm tra nếu việc trừ ngày vượt quá số ngày còn lại
        // remaining_days sẽ được tính lại trong entity sau khi daysTaken được set
        // BigDecimal currentRemaining = (balance.getTotalDaysAllocated() != null ? balance.getTotalDaysAllocated() : BigDecimal.ZERO).subtract(currentDaysTaken);
        // if (request.getDaysToDeduct().compareTo(currentRemaining) > 0) {
        //     throw new InvalidOperationException("Cannot deduct " + request.getDaysToDeduct() + " days. Remaining balance is " + currentRemaining);
        // }
        // Tốt hơn là kiểm tra với remaining_days đã tính toán sẵn
//        balance.calculateRemainingDays(); // Đảm bảo remaining_days là mới nhất
//        if (request.getDaysToDeduct().compareTo(balance.getRemainingDays()) > 0 && request.getDaysToDeduct().compareTo(BigDecimal.ZERO) > 0) {
//            throw new InvalidOperationException("Not enough leave balance. Requested: " + request.getDaysToDeduct() + ", Remaining: " + balance.getRemainingDays());
//        }


//        balance.setDaysTaken(newDaysTaken);
        balance.setLastBalanceUpdateAt(LocalDateTime.now());
        // remaining_days sẽ được tự động tính toán lại bằng @PostUpdate trong Entity nếu có thay đổi
        balanceRepository.save(balance);

        // Log to history
        EmployeeLeaveHistory history = new EmployeeLeaveHistory();
        history.setEmployee(employee);
        history.setOriginalLeaveRequestId(request.getOriginalLeaveRequestId());
        history.setLeaveType(leaveType);
        history.setActualLeaveStartDate(request.getActualLeaveStartDate());
        history.setActualLeaveEndDate(request.getActualLeaveEndDate());
        history.setActualDaysOfLeave(request.getDaysToDeduct());
        history.setReasonForApprovedLeave(request.getApprovedReason());
        history.setRequestStatusWhenLogged("APPROVED_COMPLETED"); // Or a status indicating it's logged after approval
        // history.setHistoryLoggedAt() will be set by @CreationTimestamp
        historyRepository.save(history);
    }

    // Hàm mapper thủ công ví dụ
    private EmployeeLeaveBalanceDto convertToLeaveBalanceDto(EmployeeLeaveBalance entity) {
        if (entity == null) return null;
        EmployeeLeaveBalanceDto dto = new EmployeeLeaveBalanceDto();
        dto.setBalanceId(entity.getBalanceId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setLeaveTypeCode(entity.getLeaveType().getLeaveTypeCode());
        dto.setLeaveTypeName(entity.getLeaveType().getLeaveTypeName());
        dto.setBalanceApplicableYear(entity.getBalanceApplicableYear());
        dto.setTotalDaysAllocated(entity.getTotalDaysAllocated());
//        dto.setDaysTaken(entity.getDaysTaken());
//        entity.calculateRemainingDays(); // Đảm bảo tính toán trước khi trả DTO
//        dto.setRemainingDays(entity.getRemainingDays());
        dto.setLastBalanceUpdateAt(entity.getLastBalanceUpdateAt());
        return dto;
    }
}
