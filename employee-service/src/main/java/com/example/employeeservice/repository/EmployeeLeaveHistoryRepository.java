package com.example.employeeservice.repository;

import com.example.employeeservice.entity.Employee;
import com.example.employeeservice.entity.EmployeeLeaveHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLeaveHistoryRepository extends JpaRepository<EmployeeLeaveHistory, String> {
    List<EmployeeLeaveHistory> findByEmployeeOrderByActualLeaveStartDateDesc(Employee employee);
}