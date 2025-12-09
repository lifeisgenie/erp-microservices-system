package com.example.erp.notification.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/approvals")
    public void sendApprovalNotification(@RequestBody ApprovalNotificationRequest request) {
        // 예: /topic/approvals/11 로 브로드캐스트
        String destination = "/topic/approvals/" + request.getRequestId();
        messagingTemplate.convertAndSend(destination, request);
    }

    @Getter
    @NoArgsConstructor
    public static class ApprovalNotificationRequest {
        private Integer requestId;
        private Integer requesterId;
        private String finalStatus;  // approved / rejected
        private String title;
    }
}