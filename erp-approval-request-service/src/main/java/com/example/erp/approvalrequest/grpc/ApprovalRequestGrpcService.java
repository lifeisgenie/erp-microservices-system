package com.example.erp.approvalrequest.grpc;

import approval.ApprovalGrpc;
import approval.ApprovalResultRequest;
import approval.ApprovalResultResponse;
import com.example.erp.approvalrequest.domain.ApprovalDocument;
import com.example.erp.approvalrequest.domain.ApprovalRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ApprovalRequestGrpcService extends ApprovalGrpc.ApprovalImplBase {

    private final ApprovalRepository approvalRepository;

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

        // 2. steps 리스트를 새로 만들면서 해당 step/approver의 status만 변경
        List<ApprovalDocument.Step> updatedSteps = doc.getSteps().stream()
                .map(s -> {
                    if (s.getStep().equals(step) && s.getApproverId().equals(approverId)) {
                        // 이 결재자의 결과 반영
                        return new ApprovalDocument.Step(
                                s.getStep(),
                                s.getApproverId(),
                                status,   // "approved" or "rejected"
                                now
                        );
                    }
                    // 나머지 step은 그대로 유지
                    return s;
                })
                .toList();

        // 3. 전체 finalStatus 계산
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

        // 4. 도메인 객체 업데이트 + 저장
        doc.updateSteps(updatedSteps, finalStatus, now);
        approvalRepository.save(doc);

        // 5. gRPC 응답
        ApprovalResultResponse response = ApprovalResultResponse.newBuilder()
                .setStatus(finalStatus)  // 최종 상태를 그대로 전달
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}