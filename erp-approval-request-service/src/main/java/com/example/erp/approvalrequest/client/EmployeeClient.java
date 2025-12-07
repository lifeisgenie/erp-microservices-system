package com.example.erp.approvalrequest.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class EmployeeClient {

    private final RestTemplate restTemplate;

    @Value("${employee.service.base-url}")
    private String employeeServiceBaseUrl;

    public boolean existsById(Integer employeeId) {
        String url = employeeServiceBaseUrl + "/employees/" + employeeId;
        try {
            restTemplate.getForEntity(url, String.class);
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }
}