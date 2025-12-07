package com.example.erp.employee.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // department, position 둘 다 필터링
    List<Employee> findByDepartmentAndPosition(String department, String position);

    // department만 필터링
    List<Employee> findByDepartment(String department);

    // position만 필터링
    List<Employee> findByPosition(String position);
}