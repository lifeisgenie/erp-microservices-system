package com.example.erp.approvalrequest.grpc;

import approval.ApprovalGrpc;
import approval.ApprovalRequest;
import approval.ApprovalResultRequest;
import approval.ApprovalResultResponse;
import com.example.erp.approvalrequest.domain.ApprovalDocument;
import com.example.erp.approvalrequest.domain.ApprovalRepository;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ApprovalRequestGrpcService extends ApprovalGrpc.ApprovalImplBase {

    private final ApprovalRepository approvalRepository;

    // Processing Service로 RequestApproval() 호출할 때 사용하는 gRPC 클라이언트
    private final ApprovalGrpc.ApprovalBlockingStub approvalProcessingBlockingStub;

    // Notification Service로 REST 호출할 때 사용할 RestTemplate
    private final RestTemplate notificationRestTemplate;

    @Value("${notification.service.base-url:http://localhost:8084}")
    private String notificationBaseUrl;

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

        // 5. 다음 결재자 자동 큐잉 (조건: 이번 결과는 approved && 전체는 아직 in_progress)
        if ("approved".equals(status) && "in_progress".equals(finalStatus)) {
            ApprovalDocument.Step nextStep = updatedSteps.stream()
                    .filter(s -> "pending".equalsIgnoreCase(s.getStatus()))
                    .min(Comparator.comparingInt(ApprovalDocument.Step::getStep))
                    .orElse(null);

            if (nextStep != null) {
                try {
                    // proto의 Step 메시지는 이름이 "Step" 이라서 approval.Step 으로 접근
                    ApprovalRequest grpcReq = ApprovalRequest.newBuilder()
                            .setRequestId(doc.getRequestId())
                            .setRequesterId(doc.getRequesterId())
                            .setTitle(doc.getTitle())
                            .setContent(doc.getContent())
                            .addAllSteps(
                                    updatedSteps.stream()
                                            .map(s -> approval.Step.newBuilder()
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
                    // 필요하다면 재시도 / DLQ 같은 패턴 여기에 추가 가능
                }
            }
        }

        // 6. 최종 상태면 Notification Service로 REST 알림 호출
        if ("approved".equals(finalStatus) || "rejected".equals(finalStatus)) {
            try {
                ApprovalNotificationPayload payload = new ApprovalNotificationPayload(
                        doc.getRequestId(),
                        doc.getRequesterId(),
                        finalStatus,
                        doc.getTitle()
                );

                notificationRestTemplate.postForEntity(
                        notificationBaseUrl + "/notifications/approvals",
                        payload,
                        Void.class
                );

                System.out.println("[Notification] sent approval notification for requestId=" +
                        doc.getRequestId() + ", finalStatus=" + finalStatus);
            } catch (Exception e) {
                e.printStackTrace();
                // 알림 실패는 비즈니스 실패로 보지 않고, 일단 로그만 남김
            }
        }

        // 7. gRPC 응답 (최종 상태 반환)
        ApprovalResultResponse response = ApprovalResultResponse.newBuilder()
                .setStatus(finalStatus)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // 내부에서 Notification REST 호출할 때 사용할 payload 클래스
    static class ApprovalNotificationPayload {
        private final Integer requestId;
        private final Integer requesterId;
        private final String finalStatus;
        private final String title;

        public ApprovalNotificationPayload(Integer requestId,
                                           Integer requesterId,
                                           String finalStatus,
                                           String title) {
            this.requestId = requestId;
            this.requesterId = requesterId;
            this.finalStatus = finalStatus;
            this.title = title;
        }

        public Integer getRequestId() {
            return requestId;
        }

        public Integer getRequesterId() {
            return requesterId;
        }

        public String getFinalStatus() {
            return finalStatus;
        }

        public String getTitle() {
            return title;
        }
    }
}