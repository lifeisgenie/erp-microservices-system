package com.example.erp.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployeeCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String department;

    @NotBlank
    private String position;
}