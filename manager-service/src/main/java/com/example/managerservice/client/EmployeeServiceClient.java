
package com.example.managerservice.client;

import com.example.managerservice.dto.ext.EmployeeDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "employee-service", path = "/api/employees")
public interface EmployeeServiceClient {

    @GetMapping("/{employeeId}")
    ResponseEntity<EmployeeDetailsDto> getEmployeeDetails(@PathVariable("employeeId") String employeeId);

    // Giả sử EmployeeService có API để lấy danh sách nhân viên báo cáo cho một quản lý
    @GetMapping // Hoặc một path cụ thể ví dụ /by-manager/{managerId}
    ResponseEntity<List<EmployeeDetailsDto>> getEmployeesByManagerId(@RequestParam("directManagerId") String managerId);
}