package com.example.erp.approvalrequest.exception;

public class ApprovalNotFoundException extends RuntimeException {

    public ApprovalNotFoundException(Integer requestId) {
        super("Approval request not found: requestId=" + requestId);
    }
}