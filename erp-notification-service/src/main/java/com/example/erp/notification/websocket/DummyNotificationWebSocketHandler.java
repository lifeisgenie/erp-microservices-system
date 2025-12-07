package com.example.erp.notification.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

@Component
public class DummyNotificationWebSocketHandler implements WebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 나중에 employeeId 세션 관리 구현 예정
        System.out.println("WebSocket connected: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        // 클라이언트에서 오는 메시지는 거의 없을 예정
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        exception.printStackTrace();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        System.out.println("WebSocket closed: " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}