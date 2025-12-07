package com.example.erp.approvalprocessing.exception;

public class PendingApprovalNotFoundException extends RuntimeException {

    public PendingApprovalNotFoundException(int approverId, int requestId) {
        super("Pending approval not found for approverId=" + approverId + ", requestId=" + requestId);
    }
}