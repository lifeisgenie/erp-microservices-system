package com.example.erp.approvalprocessing.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class PendingApproval {

    private int requestId;
    private int requesterId;
    private String title;
    private String content;
    private List<Step> steps = new ArrayList<>(); // proto의 steps와 유사 구조

    public PendingApproval(int requestId, int requesterId, String title, String content, List<Step> steps) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.title = title;
        this.content = content;
        this.steps = steps;
    }

    @Getter
    @NoArgsConstructor
    public static class Step {
        private int step;
        private int approverId;
        private String status; // pending, approved, rejected

        public Step(int step, int approverId, String status) {
            this.step = step;
            this.approverId = approverId;
            this.status = status;
        }
    }
}