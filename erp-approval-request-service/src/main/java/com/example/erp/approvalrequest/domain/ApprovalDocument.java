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

    private Integer requestId;
    private Integer requesterId;
    private String title;
    private String content;
    private List<Step> steps;
    private String finalStatus;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @NoArgsConstructor
    public static class Step {
        private Integer step;
        private Integer approverId;
        private String status;
        private Instant updatedAt;
    }
}