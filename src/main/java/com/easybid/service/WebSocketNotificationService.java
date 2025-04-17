package com.easybid.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(String email, String message) {
        messagingTemplate.convertAndSendToUser(
                email,
                "/queue/notifications", // 클라이언트 구독 경로
                message
        );
    }
}
