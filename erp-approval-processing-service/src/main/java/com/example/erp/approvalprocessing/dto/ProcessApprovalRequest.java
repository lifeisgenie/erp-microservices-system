package com.example.erp.approvalprocessing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProcessApprovalRequest {

    @NotBlank
    private String status; // "approved" or "rejected"
}