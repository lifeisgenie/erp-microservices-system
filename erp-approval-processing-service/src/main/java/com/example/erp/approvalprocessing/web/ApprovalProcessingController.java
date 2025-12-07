package com.example.erp.approvalprocessing.web;

import com.example.erp.approvalprocessing.dto.PendingApprovalResponse;
import com.example.erp.approvalprocessing.dto.ProcessApprovalRequest;
import com.example.erp.approvalprocessing.service.ApprovalProcessingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/process")
@RequiredArgsConstructor
public class ApprovalProcessingController {

    private final ApprovalProcessingService approvalProcessingService;

    // GET /process/{approverId}
    @GetMapping("/{approverId}")
    public ResponseEntity<List<PendingApprovalResponse>> getPendingApprovals(
            @PathVariable int approverId
    ) {
        List<PendingApprovalResponse> list = approvalProcessingService.getPendingApprovals(approverId);
        return ResponseEntity.ok(list);
    }

    // POST /process/{approverId}/{requestId}
    @PostMapping("/{approverId}/{requestId}")
    public ResponseEntity<Void> processApproval(
            @PathVariable int approverId,
            @PathVariable int requestId,
            @Valid @RequestBody ProcessApprovalRequest request
    ) {
        approvalProcessingService.processApproval(approverId, requestId, request);
        return ResponseEntity.noContent().build();
    }
}