package com.example.erp.approvalrequest.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ApprovalStepResponse {

    private Integer step;
    private Integer approverId;
    private String status;
    private Instant updatedAt;
}