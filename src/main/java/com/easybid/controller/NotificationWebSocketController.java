package com.easybid.controller;

import com.easybid.entity.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 메시지 수동 발송 (테스트용)
     */
    @MessageMapping("/notify") // /app/notify 로 수신
    public void broadcast(NotificationMessage message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }

    /**
     * 특정 사용자에게 전송
     */
    public void sendToUser(String email, NotificationMessage message) {
        messagingTemplate.convertAndSendToUser(email, "/queue/notifications", message);
    }
}
