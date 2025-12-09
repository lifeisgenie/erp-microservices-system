package com.example.erp.approvalprocessing.grpc;

import approval.ApprovalGrpc;
import approval.ApprovalRequest;
import approval.ApprovalResponse;
import com.example.erp.approvalprocessing.domain.PendingApproval;
import com.example.erp.approvalprocessing.service.ApprovalQueueService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ApprovalProcessingGrpcService extends ApprovalGrpc.ApprovalImplBase {

    private final ApprovalQueueService approvalQueueService;

    @Override
    public void requestApproval(ApprovalRequest request,
                                StreamObserver<ApprovalResponse> responseObserver) {

        // 1. steps 중 첫 번째 pending 상태의 approverId 찾기
        var targetStep = request.getStepsList().stream()
                .filter(s -> "pending".equalsIgnoreCase(s.getStatus()))
                .findFirst()
                .orElse(null);

        if (targetStep == null) {
            ApprovalResponse response = ApprovalResponse.newBuilder()
                    .setStatus("no_pending_step")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        int approverId = targetStep.getApproverId();

        // 2. PendingApproval로 변환
        List<PendingApproval.Step> steps = request.getStepsList().stream()
                .map(s -> new PendingApproval.Step(
                        s.getStep(),
                        s.getApproverId(),
                        s.getStatus()
                ))
                .toList();

        PendingApproval pending = new PendingApproval(
                request.getRequestId(),
                request.getRequesterId(),
                request.getTitle(),
                request.getContent(),
                steps
        );

        // 3. 인메모리 큐에 저장
        approvalQueueService.enqueueForApprover(approverId, pending);

        // 4. 응답
        ApprovalResponse response = ApprovalResponse.newBuilder()
                .setStatus("received")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}