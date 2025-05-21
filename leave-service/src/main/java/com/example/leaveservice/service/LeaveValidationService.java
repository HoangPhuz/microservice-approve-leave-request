

package com.example.leaveservice.service;

import com.example.leaveservice.client.EmployeeServiceClient;
import com.example.leaveservice.dto.LeaveValidationRequest;
import com.example.leaveservice.dto.LeaveValidationResponse;
import com.example.leaveservice.dto.EmployeeLeaveBalanceDto;
import com.example.leaveservice.dto.EmployeeLeaveHistoryDto;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveValidationService {
//    private static final Logger logger = LoggerFactory.getLogger(LeaveValidationService.class);
//
//    private final EmployeeServiceClient employeeServiceClient;
//
//    public LeaveValidationService(EmployeeServiceClient employeeServiceClient
//                                  ) {
//        this.employeeServiceClient = employeeServiceClient;
//    }
//
//    public LeaveValidationResponse validateLeaveRequest(LeaveValidationRequest request) {
//        logger.info("Validating leave request for employee: {}, type: {}, year: {}",
//                request.getEmployeeId(), request.getLeaveTypeCode(), request.getForYear());
//
//        LeaveValidationResponse response = new LeaveValidationResponse();
//        response.setValid(true); // Assume valid initially
//
//        // Bước 1: Lấy số dư ngày nghỉ được CẤP (allocated) và SỐ NGÀY ĐÃ NGHỈ từ EmployeeService
//        EmployeeLeaveBalanceDto balanceData = getEmployeeLeaveBalance(request.getEmployeeId(), request.getLeaveTypeCode(), request.getForYear());
//
//        BigDecimal totalAllocatedDays = (balanceData != null && balanceData.getTotalDaysAllocated() != null)
//                ? balanceData.getTotalDaysAllocated() : BigDecimal.ZERO;
//        BigDecimal daysAlreadyTaken = (balanceData != null && balanceData.getDaysTaken() != null)
//                ? balanceData.getDaysTaken() : BigDecimal.ZERO;
//
//        // Nếu EmployeeService không trả về 'daysTaken', chúng ta cần tính từ lịch sử.
//        // Điều này sẽ kém hiệu quả hơn và phụ thuộc vào API của EmployeeService có trả đủ lịch sử không.
//        // Giả định ở đây là EmployeeService đã tính toán và trả về 'daysTaken' trong DTO của nó.
//        // Nếu không, cần gọi API getLeaveHistoryForEmployee và tính tổng:
//        // List<EmployeeLeaveHistoryDto> history = getEmployeeLeaveHistory(request.getEmployeeId(), request.getLeaveTypeCode(), request.getForYear());
//        // daysAlreadyTaken = calculateDaysTakenFromHistory(history);
//
//        BigDecimal currentRemainingDays = totalAllocatedDays.subtract(daysAlreadyTaken);
//        response.setCalculatedRemainingDays(currentRemainingDays);
//
//        // Bước 2: Tính số ngày làm việc thực tế trong yêu cầu (trừ T7, CN, ngày lễ nếu có)
//        BigDecimal businessDaysInRequest = calculateBusinessDays(request.getRequestedStartDate(), request.getRequestedEndDate());
//        response.setCalculatedBusinessDaysInRequest(businessDaysInRequest);
//
//        // Bước 3: Kiểm tra tính hợp lệ
//        // So sánh số ngày yêu cầu (đã tính toán theo ngày làm việc) với số ngày còn lại
//        if (businessDaysInRequest.compareTo(currentRemainingDays) > 0) {
//            response.setValid(false);
//            response.getValidationMessages().add(
//                    String.format("Requested business days (%.2f) exceed remaining leave balance (%.2f).",
//                            businessDaysInRequest, currentRemainingDays)
//            );
//        }
//
//        if (businessDaysInRequest.compareTo(BigDecimal.ZERO) <= 0 && request.getRequestedDays().compareTo(BigDecimal.ZERO) > 0) {
//            response.setValid(false);
//            response.getValidationMessages().add("Calculated business days in request is zero or negative, but requested days is positive. Please check dates.");
//        }
//
//
//        // (Tùy chọn) Thêm các quy tắc kiểm tra chính sách khác ở đây nếu LeaveService đảm nhiệm
//        // Ví dụ: kiểm tra số ngày báo trước, số ngày nghỉ tối đa mỗi lần, v.v.
//        // if (isAdvanceNoticeViolated(request)) {
//        //     response.setValid(false);
//        //     response.getValidationMessages().add("Advance notice period not met.");
//        // }
//
//        logger.info("Validation result for employee {}: isValid={}, messages={}, remainingDays={}, businessDaysInRequest={}",
//                request.getEmployeeId(), response.isValid(), response.getValidationMessages(),
//                response.getCalculatedRemainingDays(), response.getCalculatedBusinessDaysInRequest());
//
//        return response;
//    }
//
//    private EmployeeLeaveBalanceDto getEmployeeLeaveBalance(String employeeId, String leaveTypeCode, int year) {
//        try {
//            ResponseEntity<EmployeeLeaveBalanceDto> feignResponse = employeeServiceClient.getSpecificLeaveBalance(employeeId, leaveTypeCode, year);
//            if (feignResponse.getStatusCode() == HttpStatus.OK) {
//                return feignResponse.getBody();
//            }
//            logger.warn("Failed to get leave balance from EmployeeService for emp: {}, type: {}, year: {}. Status: {}",
//                    employeeId, leaveTypeCode, year, feignResponse.getStatusCode());
//        } catch (FeignException.NotFound e) {
//            logger.warn("Leave balance not found in EmployeeService for emp: {}, type: {}, year: {}. Assuming zero.", employeeId, leaveTypeCode, year);
//        } catch (FeignException e) {
//            logger.error("Error calling EmployeeService for leave balance (emp: {}, type: {}, year: {}): {}",
//                    employeeId, leaveTypeCode, year, e.getMessage());
//        }
//        // Trả về DTO mặc định nếu không lấy được hoặc lỗi, để quy trình có thể tiếp tục xử lý (ví dụ, báo lỗi "không có thông tin số dư")
//        EmployeeLeaveBalanceDto defaultDto = new EmployeeLeaveBalanceDto();
//        defaultDto.setTotalDaysAllocated(BigDecimal.ZERO);
//        defaultDto.setDaysTaken(BigDecimal.ZERO);
//        defaultDto.setRemainingDays(BigDecimal.ZERO);
//        return defaultDto;
//    }
//
//    // Hàm này có thể không cần nếu EmployeeService đã cung cấp `daysTaken` trong `EmployeeLeaveBalanceDto`
//    private List<EmployeeLeaveHistoryDto> getEmployeeLeaveHistory(String employeeId, String leaveTypeCode, int year) {
//        try {
//            ResponseEntity<List<EmployeeLeaveHistoryDto>> feignResponse = employeeServiceClient.getLeaveHistoryForEmployee(employeeId);
//            // Nếu EmployeeService API không hỗ trợ lọc theo năm và loại, phải lọc ở đây
//            if (feignResponse.getStatusCode() == HttpStatus.OK && feignResponse.getBody() != null) {
//                return feignResponse.getBody().stream()
//                        .filter(h -> h.getActualLeaveStartDate() != null &&
//                                h.getActualLeaveStartDate().getYear() == year &&
//                                leaveTypeCode.equals(h.getLeaveTypeCode()))
//                        .collect(Collectors.toList());
//            }
//            logger.warn("Failed to get leave history from EmployeeService for emp: {}. Status: {}", employeeId, feignResponse.getStatusCode());
//        } catch (FeignException e) {
//            logger.error("Error calling EmployeeService for leave history (emp: {}): {}", employeeId, e.getMessage());
//        }
//        return Collections.emptyList();
//    }
//
//    private BigDecimal calculateDaysTakenFromHistory(List<EmployeeLeaveHistoryDto> historyList) {
//        if (historyList == null) return BigDecimal.ZERO;
//        return historyList.stream()
//                .map(EmployeeLeaveHistoryDto::getActualDaysOfLeave)
//                .filter(java.util.Objects::nonNull)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
//
//    public BigDecimal calculateBusinessDays(final LocalDate startDate, final LocalDate endDate) {
//        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
//            return BigDecimal.ZERO;
//        }
//
//
//
//        long businessDaysCount = 0;
//        LocalDate current = startDate;
//        while (!current.isAfter(endDate)) {
//            if (!(current.getDayOfWeek() == DayOfWeek.SATURDAY ||
//                    current.getDayOfWeek() == DayOfWeek.SUNDAY
//                    )) {
//                businessDaysCount++;
//            }
//            current = current.plusDays(1);
//        }
//        return new BigDecimal(businessDaysCount);
//    }

    private static final Logger logger = LoggerFactory.getLogger(LeaveValidationService.class);

    private final EmployeeServiceClient employeeServiceClient;

    public LeaveValidationService(EmployeeServiceClient employeeServiceClient) {
        this.employeeServiceClient = employeeServiceClient;
    }

    public LeaveValidationResponse validateLeaveRequest(LeaveValidationRequest request) {
        logger.info("Validating leave request for employee: {}, type: {}, year: {}",
                request.getEmployeeId(), request.getLeaveTypeCode(), request.getForYear());

        LeaveValidationResponse response = new LeaveValidationResponse();
        response.setValid(true);

        // Bước 1: Lấy số dư ngày nghỉ được CẤP (allocated) từ EmployeeService
        EmployeeLeaveBalanceDto balanceData = getEmployeeLeaveBalanceFromEmployeeService(
                request.getEmployeeId(), request.getLeaveTypeCode(), request.getForYear()
        );
        BigDecimal totalAllocatedDays = (balanceData != null && balanceData.getTotalDaysAllocated() != null)
                ? balanceData.getTotalDaysAllocated() : BigDecimal.ZERO;

        // Bước 2: Lấy toàn bộ lịch sử nghỉ phép của nhân viên từ EmployeeService
        List<EmployeeLeaveHistoryDto> allEmployeeHistory = getEmployeeLeaveHistoryFromEmployeeService(request.getEmployeeId());

        // Bước 3: Tính toán tổng số ngày đã nghỉ (daysTaken) cho loại nghỉ và năm cụ thể từ lịch sử
        BigDecimal daysTakenThisYear = calculateTotalDaysTakenForTypeAndYear(
                allEmployeeHistory, request.getLeaveTypeCode(), request.getForYear()
        );

        // Bước 4: Tính số ngày còn lại
        BigDecimal calculatedRemainingDays = totalAllocatedDays.subtract(daysTakenThisYear);
        response.setCalculatedRemainingDays(calculatedRemainingDays);

        // Bước 5: Tính số ngày làm việc thực tế trong yêu cầu (trừ T7, CN, ngày lễ nếu có)
        BigDecimal businessDaysInRequest = calculateBusinessDays(request.getRequestedStartDate(), request.getRequestedEndDate());
        response.setCalculatedBusinessDaysInRequest(businessDaysInRequest);

        // Bước 6: Kiểm tra tính hợp lệ
        if (businessDaysInRequest.compareTo(calculatedRemainingDays) > 0) {
            response.setValid(false);
            response.getValidationMessages().add(
                    String.format("Requested business days (%.2f) exceed calculated remaining leave balance (%.2f). Allocated: %.2f, Taken so far: %.2f.",
                            businessDaysInRequest, calculatedRemainingDays, totalAllocatedDays, daysTakenThisYear)
            );
        }

        if (businessDaysInRequest.compareTo(BigDecimal.ZERO) <= 0) {
            response.setValid(false);
            response.getValidationMessages().add("Calculated business days in request is zero or negative, while requested days is positive. Please check dates.");
        }

        // (Tùy chọn) Thêm các quy tắc kiểm tra chính sách khác ở đây
        // ...

        logger.info("Validation result for employee {}: isValid={}, messages={}, calculatedRemainingDays={}, businessDaysInRequest={}",
                request.getEmployeeId(), response.isValid(), response.getValidationMessages(),
                response.getCalculatedRemainingDays(), response.getCalculatedBusinessDaysInRequest());

        return response;
    }

    private EmployeeLeaveBalanceDto getEmployeeLeaveBalanceFromEmployeeService(String employeeId, String leaveTypeCode, int year) {
        try {
            ResponseEntity<EmployeeLeaveBalanceDto> feignResponse = employeeServiceClient.getSpecificLeaveBalance(employeeId, leaveTypeCode, year);
            if (feignResponse.getStatusCode() == HttpStatus.OK && feignResponse.getBody() != null) {
                return feignResponse.getBody();
            }
            logger.warn("Failed to get leave balance from EmployeeService for emp: {}, type: {}, year: {}. Status: {}",
                    employeeId, leaveTypeCode, year, feignResponse.getStatusCode());
        } catch (FeignException.NotFound e) {
            logger.warn("Leave balance not found in EmployeeService for emp: {}, type: {}, year: {}. Assuming zero allocation.", employeeId, leaveTypeCode, year);
        } catch (FeignException e) {
            logger.error("Error calling EmployeeService for leave balance (emp: {}, type: {}, year: {}): Status {}, Message: {}",
                    employeeId, leaveTypeCode, year, e.status(), e.contentUTF8(), e); // Log content lỗi
        }
        // Trả về DTO mặc định nếu không lấy được hoặc lỗi
        EmployeeLeaveBalanceDto defaultDto = new EmployeeLeaveBalanceDto();
        defaultDto.setTotalDaysAllocated(BigDecimal.ZERO);
        return defaultDto;
    }

    private List<EmployeeLeaveHistoryDto> getEmployeeLeaveHistoryFromEmployeeService(String employeeId) {
        try {
            ResponseEntity<List<EmployeeLeaveHistoryDto>> feignResponse = employeeServiceClient.getLeaveHistoryForEmployee(employeeId);
            if (feignResponse.getStatusCode() == HttpStatus.OK && feignResponse.getBody() != null) {
                return feignResponse.getBody();
            }
            logger.warn("Failed to get leave history from EmployeeService for emp: {}. Status: {}", employeeId, feignResponse.getStatusCode());
        } catch (FeignException e) {
            logger.error("Error calling EmployeeService for leave history (emp: {}): Status {}, Message: {}",
                    employeeId, e.status(), e.contentUTF8(), e); // Log content lỗi
        }
        return Collections.emptyList();
    }

    private BigDecimal calculateTotalDaysTakenForTypeAndYear(List<EmployeeLeaveHistoryDto> allHistory, String leaveTypeCode, int year) {
        if (allHistory == null) {
            return BigDecimal.ZERO;
        }
        return allHistory.stream()
                .filter(h -> h.getActualLeaveStartDate() != null &&
                        h.getActualLeaveStartDate().getYear() == year &&
                        leaveTypeCode.equals(h.getLeaveTypeCode()) &&
                        "APPROVED_COMPLETED".equalsIgnoreCase(h.getRequestStatusWhenLogged())) // Chỉ tính các yêu cầu đã hoàn thành
                .map(h -> h.getActualDaysOfLeave() != null ? h.getActualDaysOfLeave() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateBusinessDays(final LocalDate startDate, final LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return BigDecimal.ZERO;
        }


        long businessDaysCount = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!(current.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    current.getDayOfWeek() == DayOfWeek.SUNDAY
                    )) {
                businessDaysCount++;
            }
            current = current.plusDays(1);
        }
        return new BigDecimal(businessDaysCount);
    }
}