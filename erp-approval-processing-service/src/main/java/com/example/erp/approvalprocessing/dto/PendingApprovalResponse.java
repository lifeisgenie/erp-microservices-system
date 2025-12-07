package com.example.erp.approvalprocessing.dto;

import com.example.erp.approvalprocessing.domain.PendingApproval;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PendingApprovalResponse {

    private int requestId;
    private int requesterId;
    private String title;
    private String content;
    private List<Step> steps;

    @Getter
    @Builder
    public static class Step {
        private int step;
        private int approverId;
        private String status;
    }

    public static PendingApprovalResponse from(PendingApproval p) {
        List<Step> steps = p.getSteps().stream()
                .map(s -> Step.builder()
                        .step(s.getStep())
                        .approverId(s.getApproverId())
                        .status(s.getStatus())
                        .build())
                .toList();

        return PendingApprovalResponse.builder()
                .requestId(p.getRequestId())
                .requesterId(p.getRequesterId())
                .title(p.getTitle())
                .content(p.getContent())
                .steps(steps)
                .build();
    }
}