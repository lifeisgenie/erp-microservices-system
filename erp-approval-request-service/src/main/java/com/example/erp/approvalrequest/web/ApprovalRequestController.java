package com.example.erp.approvalrequest.web;

import com.example.erp.approvalrequest.dto.*;
import com.example.erp.approvalrequest.service.ApprovalRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/approvals")
@RequiredArgsConstructor
public class ApprovalRequestController {

    private final ApprovalRequestService approvalRequestService;

    // POST /approvals : 결재 요청 생성
    @PostMapping
    public ResponseEntity<ApprovalCreatedResponse> createApproval(
            @Valid @RequestBody ApprovalCreateRequest request
    ) {
        ApprovalCreatedResponse response = approvalRequestService.createApproval(request);
        return ResponseEntity
                .created(URI.create("/approvals/" + response.getRequestId()))
                .body(response);
    }

    // GET /approvals : 전체 목록
    @GetMapping
    public ResponseEntity<List<ApprovalSummaryResponse>> getApprovals() {
        List<ApprovalSummaryResponse> approvals = approvalRequestService.getApprovals();
        return ResponseEntity.ok(approvals);
    }

    // GET /approvals/{requestId} : 상세 조회
    @GetMapping("/{requestId}")
    public ResponseEntity<ApprovalDetailResponse> getApproval(
            @PathVariable Integer requestId
    ) {
        ApprovalDetailResponse detail = approvalRequestService.getApproval(requestId);
        return ResponseEntity.ok(detail);
    }
}