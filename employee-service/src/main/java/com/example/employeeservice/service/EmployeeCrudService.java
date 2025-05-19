package com.example.employeeservice.service;

import com.example.employeeservice.dto.EmployeeDto;
import com.example.employeeservice.entity.Department;
import com.example.employeeservice.entity.Employee;
import com.example.employeeservice.exception.ResourceNotFoundException;
import com.example.employeeservice.repository.DepartmentRepository;
import com.example.employeeservice.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// Sử dụng MapStruct hoặc viết hàm mapper thủ công
// import com.yourcompany.employeeservice.mapper.EmployeeMapper;

@Service
public class EmployeeCrudService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository; // Thêm
    // private final EmployeeMapper employeeMapper;

    public EmployeeCrudService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository /*, EmployeeMapper employeeMapper*/) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository; // Thêm
        // this.employeeMapper = employeeMapper;
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeDetails(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return convertToEmployeeDto(employee); // employeeMapper.toDto(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeManagerDetails(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        if (employee.getDirectManagerId() == null) {
            throw new ResourceNotFoundException("Manager not found for employee id: " + employeeId);
        }
        Employee manager = employeeRepository.findById(employee.getDirectManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager details not found for manager id: " + employee.getDirectManagerId()));
        return convertToEmployeeDto(manager); // employeeMapper.toDto(manager);
    }

    // Hàm mapper thủ công ví dụ
    private EmployeeDto convertToEmployeeDto(Employee employee) {
        if (employee == null) return null;
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setFullName(employee.getFullName());
        dto.setEmailAddress(employee.getEmailAddress());
        if (employee.getDepartmentId() != null) {
            dto.setDepartmentId(employee.getDepartmentId());
            // Lấy tên phòng ban nếu cần
            Department dept = departmentRepository.findById(employee.getDepartmentId()).orElse(null);
            if (dept != null) {
                dto.setDepartmentName(dept.getDepartmentName());
            }
        }
        if (employee.getDirectManagerId() != null) {
            dto.setDirectManagerId(employee.getDirectManagerId());
            // Lấy tên quản lý nếu cần
            Employee manager = employeeRepository.findById(employee.getDirectManagerId()).orElse(null);
            if (manager != null) {
                dto.setDirectManagerName(manager.getFullName());
            }
        }
        dto.setJobTitle(employee.getJobTitle());
        dto.setHireDate(employee.getHireDate());
        return dto;
    }
    // Thêm các phương thức CRUD khác cho Employee, Department...
}
