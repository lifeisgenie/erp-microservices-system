package com.example.erp.approvalprocessing.grpc;

import approval.ApprovalGrpc;
import approval.ApprovalOuterClass;
import com.example.erp.approvalprocessing.domain.PendingApproval;
import com.example.erp.approvalprocessing.service.ApprovalQueueService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.lognet.springboot.grpc.GRpcService;

import java.util.List;

@GRpcService
@RequiredArgsConstructor
public class ApprovalProcessingGrpcService extends ApprovalGrpc.ApprovalImplBase {

    private final ApprovalQueueService approvalQueueService;

    @Override
    public void requestApproval(ApprovalOuterClass.ApprovalRequest request,
                                StreamObserver<ApprovalOuterClass.ApprovalResponse> responseObserver) {

        // 1. steps 중 첫 번째 pending 상태의 approverId 찾기
        ApprovalOuterClass.Step targetStep = request.getStepsList().stream()
                .filter(s -> "pending".equalsIgnoreCase(s.getStatus()))
                .findFirst()
                .orElse(null);

        if (targetStep == null) {
            // pending 단계가 없으면 에러 응답
            ApprovalOuterClass.ApprovalResponse response = ApprovalOuterClass.ApprovalResponse.newBuilder()
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
        ApprovalOuterClass.ApprovalResponse response = ApprovalOuterClass.ApprovalResponse.newBuilder()
                .setStatus("received")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ReturnApprovalResult는 Approval Request Service 쪽에서 gRPC 서버를 구현할 예정이므로,
    // 이 Processing Service에서는 requestApproval만 서버로 구현한다.
}