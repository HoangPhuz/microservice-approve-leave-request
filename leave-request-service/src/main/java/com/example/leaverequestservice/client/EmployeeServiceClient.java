
package com.example.leaverequestservice.client;

import com.example.leaverequestservice.dto.ext.EmployeeDetailsDto; // Cần định nghĩa DTO này
import com.example.leaverequestservice.dto.ext.UpdateBalanceAndLogHistoryCmd; // DTO đã tạo ở EmployeeService
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Giả sử EmployeeService có các API này
@FeignClient(name = "employee-service", path = "/api/employees")
public interface EmployeeServiceClient {

    @GetMapping("/{employeeId}")
    ResponseEntity<EmployeeDetailsDto> getEmployeeDetails(@PathVariable("employeeId") String employeeId);

    // API để cập nhật số dư và lịch sử nghỉ (đã được thiết kế ở EmployeeService)
    @PostMapping("/internal/balances/update-and-log") // Khớp với EmployeeService controller
    ResponseEntity<Void> updateBalanceAndLogHistory(@RequestBody UpdateBalanceAndLogHistoryCmd command);
}