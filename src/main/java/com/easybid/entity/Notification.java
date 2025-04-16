package com.easybid.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 수신자 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 알림 유형 (ex: 판매 완료, 낙찰 성공, 판매 실패 등)
    @Column(nullable = false, length = 50)
    private String type;

    // 알림 메시지 (내용)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    // 알림 생성 시간
    @Column(name = "notified_at", nullable = false)
    private LocalDateTime notifiedAt;

    // 읽음 여부
    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @PrePersist
    protected void onCreate() {
        this.notifiedAt = LocalDateTime.now();
        this.isRead = false;
    }
}
