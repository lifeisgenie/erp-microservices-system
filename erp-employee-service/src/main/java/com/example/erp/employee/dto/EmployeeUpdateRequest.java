package com.example.erp.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployeeUpdateRequest {

    // name은 여기서 받지 않는다 (수정 불가)
    @NotBlank
    private String department;

    @NotBlank
    private String position;
}