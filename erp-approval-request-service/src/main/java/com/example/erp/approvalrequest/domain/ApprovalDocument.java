package com.example.erp.approvalrequest.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@Document(collection = "approvals")
public class ApprovalDocument {

    @Id
    private String id; // Mongo ObjectId

    private Integer requestId;   // 자동 증가 시퀀스
    private Integer requesterId;
    private String title;
    private String content;
    private List<Step> steps;
    private String finalStatus;  // in_progress, approved, rejected
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @NoArgsConstructor
    public static class Step {
        private Integer step;
        private Integer approverId;
        private String status;    // pending, approved, rejected
        private Instant updatedAt;

        public Step(Integer step, Integer approverId, String status, Instant updatedAt) {
            this.step = step;
            this.approverId = approverId;
            this.status = status;
            this.updatedAt = updatedAt;
        }
    }

    public ApprovalDocument(
            Integer requestId,
            Integer requesterId,
            String title,
            String content,
            List<Step> steps,
            String finalStatus,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.title = title;
        this.content = content;
        this.steps = steps;
        this.finalStatus = finalStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateSteps(List<Step> newSteps, String newFinalStatus, Instant updatedAt) {
        this.steps = newSteps;
        this.finalStatus = newFinalStatus;
        this.updatedAt = updatedAt;
    }
}