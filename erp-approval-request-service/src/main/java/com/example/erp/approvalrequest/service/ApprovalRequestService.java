package com.example.erp.approvalrequest.service;

import approval.ApprovalGrpc;
import approval.ApprovalOuterClass;
import com.example.erp.approvalrequest.client.EmployeeClient;
import com.example.erp.approvalrequest.domain.ApprovalDocument;
import com.example.erp.approvalrequest.domain.ApprovalRepository;
import com.example.erp.approvalrequest.dto.*;
import com.example.erp.approvalrequest.exception.ApprovalNotFoundException;
import com.example.erp.approvalrequest.exception.InvalidApprovalRequestException;
import com.example.erp.approvalrequest.sequence.SequenceGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalRequestService {

    private static final String APPROVAL_REQUEST_SEQ_NAME = "approval_request_seq";

    private final ApprovalRepository approvalRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final EmployeeClient employeeClient;
    private final ApprovalGrpc.ApprovalBlockingStub approvalBlockingStub;

    // 1) 결재 요청 생성
    public ApprovalCreatedResponse createApproval(ApprovalCreateRequest req) {

        // 1. 직원 존재 여부 검증 (requester + approvers)
        validateEmployees(req);

        // 2. steps 정렬/검증 (1부터 오름차순)
        List<ApprovalCreateRequest.StepRequest> normalizedSteps = validateAndNormalizeSteps(req.getSteps());

        // 3. Mongo 시퀀스로 requestId 생성
        int requestId = sequenceGeneratorService.getNextSequence(APPROVAL_REQUEST_SEQ_NAME);

        Instant now = Instant.now();

        // 4. Document 생성
        List<ApprovalDocument.Step> docSteps = normalizedSteps.stream()
                .map(s -> new ApprovalDocument.Step(
                        s.getStep(),
                        s.getApproverId(),
                        "pending",
                        null
                ))
                .toList();

        ApprovalDocument doc = new ApprovalDocument(
                requestId,
                req.getRequesterId(),
                req.getTitle(),
                req.getContent(),
                docSteps,
                "in_progress",
                now,
                now
        );

        approvalRepository.save(doc);

        // 5. gRPC RequestApproval 호출
        callGrpcRequestApproval(doc);

        // 6. 클라이언트 응답
        return new ApprovalCreatedResponse(requestId);
    }

    private void validateEmployees(ApprovalCreateRequest req) {

        if (!employeeClient.existsById(req.getRequesterId())) {
            throw new InvalidApprovalRequestException("Invalid requesterId: " + req.getRequesterId());
        }

        for (ApprovalCreateRequest.StepRequest step : req.getSteps()) {
            Integer approverId = step.getApproverId();
            if (!employeeClient.existsById(approverId)) {
                throw new InvalidApprovalRequestException("Invalid approverId: " + approverId);
            }
        }
    }

    private List<ApprovalCreateRequest.StepRequest> validateAndNormalizeSteps(
            List<ApprovalCreateRequest.StepRequest> steps
    ) {
        if (steps == null || steps.isEmpty()) {
            throw new InvalidApprovalRequestException("steps must not be empty");
        }

        List<ApprovalCreateRequest.StepRequest> sorted = steps.stream()
                .sorted(Comparator.comparingInt(ApprovalCreateRequest.StepRequest::getStep))
                .toList();

        int expected = 1;
        for (ApprovalCreateRequest.StepRequest step : sorted) {
            if (!step.getStep().equals(expected)) {
                throw new InvalidApprovalRequestException("steps must start from 1 and be continuous. expected=" + expected);
            }
            expected++;
        }

        return sorted;
    }

    private void callGrpcRequestApproval(ApprovalDocument doc) {

        // ApprovalRequest proto 메시지 구성
        List<ApprovalOuterClass.Step> protoSteps = doc.getSteps().stream()
                .map(s -> ApprovalOuterClass.Step.newBuilder()
                        .setStep(s.getStep())
                        .setApproverId(s.getApproverId())
                        .setStatus(s.getStatus())
                        .build())
                .toList();

        ApprovalOuterClass.ApprovalRequest request = ApprovalOuterClass.ApprovalRequest.newBuilder()
                .setRequestId(doc.getRequestId())
                .setRequesterId(doc.getRequesterId())
                .setTitle(doc.getTitle())
                .setContent(doc.getContent())
                .addAllSteps(protoSteps)
                .build();

        ApprovalOuterClass.ApprovalResponse response = approvalBlockingStub.requestApproval(request);
        // response.getStatus() 가 "received" 같은 값일 것. 여기서는 로그 정도만 찍어도 됨.
        System.out.println("gRPC RequestApproval response: " + response.getStatus());
    }

    // 2) 전체 목록 조회
    @Transactional(readOnly = true)
    public List<ApprovalSummaryResponse> getApprovals() {
        return approvalRepository.findAll().stream()
                .map(ApprovalMapper::toSummaryResponse)
                .toList();
    }

    // 3) 상세 조회
    @Transactional(readOnly = true)
    public ApprovalDetailResponse getApproval(Integer requestId) {
        ApprovalDocument doc = approvalRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ApprovalNotFoundException(requestId));
        return toDetailResponse(doc);
    }
}