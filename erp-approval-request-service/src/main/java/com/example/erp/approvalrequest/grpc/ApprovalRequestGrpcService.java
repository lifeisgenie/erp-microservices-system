package com.example.erp.approvalrequest.grpc;

import approval.ApprovalGrpc;
import approval.ApprovalResultRequest;
import approval.ApprovalResultResponse;
import approval.ApprovalRequest;  
import approval.ApprovalStep;   

import com.example.erp.approvalrequest.domain.ApprovalDocument;
import com.example.erp.approvalrequest.domain.ApprovalRepository;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ApprovalRequestGrpcService extends ApprovalGrpc.ApprovalImplBase {

    private final ApprovalRepository approvalRepository;
    private final ApprovalGrpc.ApprovalBlockingStub approvalProcessingBlockingStub; 
    private final WebClient notificationWebClient;

    @Override
    public void returnApprovalResult(ApprovalResultRequest request,
                                     StreamObserver<ApprovalResultResponse> responseObserver) {

        int requestId = request.getRequestId();
        int step = request.getStep();
        int approverId = request.getApproverId();
        String status = request.getStatus().toLowerCase(Locale.ROOT); // "approved" or "rejected"

        if (!"approved".equals(status) && !"rejected".equals(status)) {
            responseObserver.onError(
                    new IllegalArgumentException("status must be 'approved' or 'rejected', but was: " + status)
            );
            return;
        }

        // 1. Mongo에서 문서 조회
        ApprovalDocument doc = approvalRepository.findByRequestId(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("ApprovalDocument not found for requestId=" + requestId));

        Instant now = Instant.now();

        // 2. steps 리스트 상태 반영
        List<ApprovalDocument.Step> updatedSteps = doc.getSteps().stream()
                .map(s -> {
                    if (s.getStep().equals(step) && s.getApproverId().equals(approverId)) {
                        return new ApprovalDocument.Step(
                                s.getStep(),
                                s.getApproverId(),
                                status,
                                now
                        );
                    }
                    return s;
                })
                .toList();

        // 3. finalStatus 계산
        boolean anyRejected = updatedSteps.stream()
                .anyMatch(s -> "rejected".equalsIgnoreCase(s.getStatus()));

        boolean allApproved = updatedSteps.stream()
                .allMatch(s -> "approved".equalsIgnoreCase(s.getStatus()));

        String finalStatus;
        if (anyRejected) {
            finalStatus = "rejected";
        } else if (allApproved) {
            finalStatus = "approved";
        } else {
            finalStatus = "in_progress";
        }

        // 4. 도메인 업데이트 + 저장
        doc.updateSteps(updatedSteps, finalStatus, now);
        approvalRepository.save(doc);
        
        // 4-1. 최종 상태면 Notification Service에 알림 전송
        if ("approved".equals(finalStatus) || "rejected".equals(finalStatus)) {
                try {
                        notificationWebClient.post()
                                .uri("/notifications/approvals")
                                .bodyValue(new ApprovalNotificationPayload(
                                        doc.getRequestId(),
                                        doc.getRequesterId(),
                                        finalStatus,
                                        doc.getTitle()
                                ))
                                .retrieve()
                                .toBodilessEntity()
                                .block();

                        System.out.println("[Notification] sent approval notification for requestId=" +
                                doc.getRequestId() + ", finalStatus=" + finalStatus);
                } catch (Exception e) {
                        e.printStackTrace();
                        // 알림 실패는 비즈니스 실패로 보지 않고 로깅만 할 수도 있음
                }
        }

        // 5. 다음 결재자 자동 큐잉 (조건: 이번 결재는 approved 이고, 아직 전체는 in_progress)
        if ("approved".equals(status) && "in_progress".equals(finalStatus)) {
            // 다음 pending step 중 step 번호 가장 낮은 것 선택
            ApprovalDocument.Step nextStep = updatedSteps.stream()
                    .filter(s -> "pending".equalsIgnoreCase(s.getStatus()))
                    .min(Comparator.comparingInt(ApprovalDocument.Step::getStep))
                    .orElse(null);

            if (nextStep != null) {
                try {
                    ApprovalRequest grpcReq = ApprovalRequest.newBuilder()
                            .setRequestId(doc.getRequestId())
                            .setRequesterId(doc.getRequesterId())
                            .setTitle(doc.getTitle())
                            .setContent(doc.getContent())
                            .addAllSteps(
                                    updatedSteps.stream()
                                            .map(s -> ApprovalStep.newBuilder()
                                                    .setStep(s.getStep())
                                                    .setApproverId(s.getApproverId())
                                                    .setStatus(s.getStatus())
                                                    .build()
                                            )
                                            .toList()
                            )
                            .build();

                    var grpcResp = approvalProcessingBlockingStub.requestApproval(grpcReq);
                    System.out.println(
                            "[ReturnApprovalResult] next approverId=" + nextStep.getApproverId()
                                    + " queued, gRPC status=" + grpcResp.getStatus()
                    );
                } catch (StatusRuntimeException e) {
                    e.printStackTrace();
                    // 필요하면 여기서 롤백/재시도 로직 추가
                }
            }
        }

        // 6. gRPC 응답 (최종 상태 반환)
        ApprovalResultResponse response = ApprovalResultResponse.newBuilder()
                .setStatus(finalStatus)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Getter
    @AllArgsConstructor
    static class ApprovalNotificationPayload {
        private Integer requestId;
        private Integer requesterId;
        private String finalStatus;
        private String title;
    }
}