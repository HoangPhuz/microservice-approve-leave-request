package com.example.employeeservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "Department", uniqueConstraints = { // Tên bảng từ file
        @UniqueConstraint(columnNames = {"department_name"})
})
public class Department {
    @Id
    @Column(name = "department_id", length = 36, nullable = false, updatable = false)
    private String departmentId; // VARCHAR(36)

    @Column(name = "department_name", length = 255, unique = true)
    private String departmentName;

    @Column(name = "department_description", columnDefinition = "TEXT")
    private String departmentDescription;


    @PrePersist
    public void initializeUUID() {
        if (departmentId == null) {
            departmentId = UUID.randomUUID().toString();
        }
    }
}
