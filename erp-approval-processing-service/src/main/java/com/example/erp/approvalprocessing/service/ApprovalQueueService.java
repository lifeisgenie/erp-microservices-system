package com.example.erp.approvalprocessing.service;

import com.example.erp.approvalprocessing.domain.PendingApproval;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApprovalQueueService {

    // key: approverId, value: 대기중인 결재 요청 리스트
    private final Map<Integer, List<PendingApproval>> queueByApprover = new ConcurrentHashMap<>();

    public void enqueueForApprover(int approverId, PendingApproval approval) {
        queueByApprover
                .computeIfAbsent(approverId, k -> new ArrayList<>())
                .add(approval);
    }

    public List<PendingApproval> getQueueForApprover(int approverId) {
        return queueByApprover.getOrDefault(approverId, Collections.emptyList());
    }

    public Optional<PendingApproval> findAndRemove(int approverId, int requestId) {
        List<PendingApproval> list = queueByApprover.get(approverId);
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }

        Iterator<PendingApproval> it = list.iterator();
        while (it.hasNext()) {
            PendingApproval p = it.next();
            if (p.getRequestId() == requestId) {
                it.remove();
                // 리스트 비면 map에서 제거
                if (list.isEmpty()) {
                    queueByApprover.remove(approverId);
                }
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }
}