package com.example.erp.employee.web;

import com.example.erp.employee.dto.*;
import com.example.erp.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // 직원 생성
    @PostMapping
    public ResponseEntity<EmployeeCreatedResponse> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request
    ) {
        EmployeeCreatedResponse response = employeeService.createEmployee(request);
        // Location 헤더도 같이
        return ResponseEntity
                .created(URI.create("/employees/" + response.getId()))
                .body(response);
    }

    // 직원 목록 조회 (필터링)
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position
    ) {
        List<EmployeeResponse> employees = employeeService.getEmployees(department, position);
        return ResponseEntity.ok(employees);
    }

    // 직원 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployee(id);
        return ResponseEntity.ok(response);
    }

    // 직원 수정 (department, position만)
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request
    ) {
        employeeService.updateEmployee(id, request);
        return ResponseEntity.noContent().build();
    }

    // 직원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}