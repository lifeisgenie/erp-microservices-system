package com.example.erp.approvalrequest.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ApprovalSummaryResponse {

    private Integer requestId;
    private Integer requesterId;
    private String title;
    private String finalStatus;
    private Instant createdAt;
    private Instant updatedAt;
}