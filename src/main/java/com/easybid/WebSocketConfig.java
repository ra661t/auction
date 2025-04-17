package com.easybid;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // 연결 주소
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS 사용 (브라우저 호환성)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // 클라이언트 구독 경로
        config.setApplicationDestinationPrefixes("/app"); // 서버 수신 경로
        config.setUserDestinationPrefix("/user"); // 사용자별 알림
    }
}
