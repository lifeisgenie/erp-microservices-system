package com.example.erp.employee.service;

import com.example.erp.employee.domain.Employee;
import com.example.erp.employee.domain.EmployeeRepository;
import com.example.erp.employee.dto.*;
import com.example.erp.employee.exception.EmployeeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.erp.employee.dto.EmployeeMapper.toEntity;
import static com.example.erp.employee.dto.EmployeeMapper.toResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeCreatedResponse createEmployee(EmployeeCreateRequest req) {
        Employee saved = employeeRepository.save(toEntity(req));
        return new EmployeeCreatedResponse(saved.getId());
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployees(String department, String position) {

        List<Employee> employees;

        if (department != null && !department.isBlank()
                && position != null && !position.isBlank()) {
            employees = employeeRepository.findByDepartmentAndPosition(department, position);
        } else if (department != null && !department.isBlank()) {
            employees = employeeRepository.findByDepartment(department);
        } else if (position != null && !position.isBlank()) {
            employees = employeeRepository.findByPosition(position);
        } else {
            employees = employeeRepository.findAll();
        }

        return employees.stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return toResponse(employee);
    }

    public void updateEmployee(Long id, EmployeeUpdateRequest req) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        // name은 수정하지 않고, department / position만 변경
        employee.updateDepartmentAndPosition(req.getDepartment(), req.getPosition());
        // JPA dirty checking으로 자동 반영
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        employeeRepository.deleteById(id);
    }
}