package com.example.erp.approvalprocessing.service;

import approval.ApprovalGrpc;
import approval.ApprovalResultRequest;
import approval.ApprovalResultResponse;
import com.example.erp.approvalprocessing.domain.PendingApproval;
import com.example.erp.approvalprocessing.dto.PendingApprovalResponse;
import com.example.erp.approvalprocessing.dto.ProcessApprovalRequest;
import com.example.erp.approvalprocessing.exception.PendingApprovalNotFoundException;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalProcessingService {

    private final ApprovalQueueService approvalQueueService;
    private final ApprovalGrpc.ApprovalBlockingStub approvalRequestBlockingStub;

    public List<PendingApprovalResponse> getPendingApprovals(int approverId) {
        return approvalQueueService.getQueueForApprover(approverId).stream()
                .map(PendingApprovalResponse::from)
                .toList();
    }

    public void processApproval(int approverId, int requestId, ProcessApprovalRequest request) {

        // 1. 인메모리 큐에서 해당 결재 건 찾고 제거
        PendingApproval pending = approvalQueueService.findAndRemove(approverId, requestId)
                .orElseThrow(() -> new PendingApprovalNotFoundException(approverId, requestId));

        String status = request.getStatus();
        if (!"approved".equalsIgnoreCase(status) && !"rejected".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("status must be 'approved' or 'rejected'");
        }

        // 2. 해당 approverId에 해당하는 step 번호 찾기
        PendingApproval.Step targetStep = pending.getSteps().stream()
                .filter(s -> s.getApproverId() == approverId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No matching step for approverId=" + approverId + " in requestId=" + requestId));

        int stepNumber = targetStep.getStep();

        // 3. gRPC로 Approval Request Service에 결과 전달
        ApprovalResultRequest grpcRequest = 
                ApprovalResultRequest.newBuilder()
                        .setRequestId(requestId)
                        .setStep(stepNumber)
                        .setApproverId(approverId)
                        .setStatus(status.toLowerCase()) // approved or rejected
                        .build();

        try {
            ApprovalResultResponse grpcResponse =
                    approvalRequestBlockingStub.returnApprovalResult(grpcRequest);
            System.out.println("ReturnApprovalResult response: " + grpcResponse.getStatus());
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to call ReturnApprovalResult via gRPC: " + e.getMessage(), e);
        }
    }
}