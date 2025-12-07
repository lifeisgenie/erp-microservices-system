package com.example.erp.employee.dto;

import com.example.erp.employee.domain.Employee;

public class EmployeeMapper {

    public static Employee toEntity(EmployeeCreateRequest req) {
        return new Employee(
                req.getName(),
                req.getDepartment(),
                req.getPosition()
        );
    }

    public static EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .createdAt(employee.getCreatedAt())
                .build();
    }
}