package com.example.erp.approvalrequest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NotificationClientConfig {

    @Bean
    public RestTemplate notificationRestTemplate() {
        return new RestTemplate();
    }
}