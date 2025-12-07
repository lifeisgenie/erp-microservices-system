package com.example.erp.approvalrequest.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ApprovalDetailResponse {

    private Integer requestId;
    private Integer requesterId;
    private String title;
    private String content;
    private List<ApprovalStepResponse> steps;
    private String finalStatus;
    private Instant createdAt;
    private Instant updatedAt;
}