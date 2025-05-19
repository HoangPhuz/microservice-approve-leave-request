


package com.example.leaveservice.client;

import com.example.leaveservice.dto.EmployeeLeaveBalanceDto;
import com.example.leaveservice.dto.EmployeeLeaveHistoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// name="employee-service" là tên đăng ký trên Eureka
// path="/api/employees" là tiền tố API của EmployeeService
@FeignClient(name = "employee-service", path = "/api/employees")
public interface EmployeeServiceClient {

    @GetMapping("/{employeeId}/leave-balances/{leaveTypeCode}")
    ResponseEntity<EmployeeLeaveBalanceDto> getSpecificLeaveBalance(
            @PathVariable("employeeId") String employeeId,
            @PathVariable("leaveTypeCode") String leaveTypeCode,
            @RequestParam("year") int year);

    // API này cần trả về lịch sử nghỉ để LeaveService tính toán daysTaken
    // Lý tưởng nhất là có thể lọc theo năm và loại nghỉ ở EmployeeService
    @GetMapping("/{employeeId}/leave-history") // Giả sử API này có sẵn
    ResponseEntity<List<EmployeeLeaveHistoryDto>> getLeaveHistoryForEmployee(
            @PathVariable("employeeId") String employeeId
            // , @RequestParam(name = "year", required = false) Integer year // Tùy chọn lọc
            // , @RequestParam(name = "leaveTypeCode", required = false) String leaveTypeCode // Tùy chọn lọc
    );
}
