package com.example.erp.employee.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EmployeeResponse {

    private Long id;
    private String name;
    private String department;
    private String position;
    private LocalDateTime createdAt;
}