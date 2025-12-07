package com.example.erp.approvalrequest.dto;

import com.example.erp.approvalrequest.domain.ApprovalDocument;

import java.util.List;

public class ApprovalMapper {

    public static ApprovalDetailResponse toDetailResponse(ApprovalDocument doc) {
        List<ApprovalStepResponse> steps = doc.getSteps().stream()
                .map(s -> ApprovalStepResponse.builder()
                        .step(s.getStep())
                        .approverId(s.getApproverId())
                        .status(s.getStatus())
                        .updatedAt(s.getUpdatedAt())
                        .build())
                .toList();

        return ApprovalDetailResponse.builder()
                .requestId(doc.getRequestId())
                .requesterId(doc.getRequesterId())
                .title(doc.getTitle())
                .content(doc.getContent())
                .steps(steps)
                .finalStatus(doc.getFinalStatus())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    public static ApprovalSummaryResponse toSummaryResponse(ApprovalDocument doc) {
        return ApprovalSummaryResponse.builder()
                .requestId(doc.getRequestId())
                .requesterId(doc.getRequesterId())
                .title(doc.getTitle())
                .finalStatus(doc.getFinalStatus())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}