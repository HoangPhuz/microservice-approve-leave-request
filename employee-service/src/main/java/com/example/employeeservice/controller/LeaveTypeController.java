package com.example.employeeservice.controller;


import com.example.employeeservice.entity.LeaveType;
import com.example.employeeservice.repository.LeaveTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController {
    private final LeaveTypeRepository leaveTypeRepository; // Hoặc một service nếu có logic

    public LeaveTypeController(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @GetMapping
    public ResponseEntity<List<LeaveType>> getAllLeaveTypes() {
        return ResponseEntity.ok(leaveTypeRepository.findAll());
    }

    @GetMapping("/{leaveTypeCode}")
    public ResponseEntity<LeaveType> getLeaveTypeByCode(@PathVariable String leaveTypeCode) {
        return leaveTypeRepository.findById(leaveTypeCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
