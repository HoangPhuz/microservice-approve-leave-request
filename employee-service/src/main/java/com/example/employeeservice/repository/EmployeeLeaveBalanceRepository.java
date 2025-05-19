package com.example.employeeservice.repository;

import com.example.employeeservice.entity.Employee;
import com.example.employeeservice.entity.EmployeeLeaveBalance;
import com.example.employeeservice.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeLeaveBalanceRepository extends JpaRepository<EmployeeLeaveBalance, String> {
    List<EmployeeLeaveBalance> findByEmployee(Employee employee);
    Optional<EmployeeLeaveBalance> findByEmployeeAndLeaveTypeAndBalanceApplicableYear(
            Employee employee, LeaveType leaveType, Integer year);
}
