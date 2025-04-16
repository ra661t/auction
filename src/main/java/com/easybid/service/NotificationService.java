package com.easybid.service;

import com.easybid.entity.Notification;
import com.easybid.entity.User;
import com.easybid.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 생성
     */
    public void createNotification(User user, String type, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    /**
     * 경매 종료 후 알림 처리 (판매자/낙찰자)
     */
    @Transactional
    public void notifyAuctionResult(User seller, String itemName, User winner) {
        if (winner == null) {
            createNotification(seller, "판매 실패", "상품 [" + itemName + "] 이(가) 낙찰되지 않았습니다.");
        } else {
            createNotification(seller, "판매 완료", "상품 [" + itemName + "] 이(가) " + winner.getName() + "님에게 낙찰되었습니다.");
            createNotification(winner, "낙찰 성공", "축하합니다! 상품 [" + itemName + "] 을(를) 낙찰받았습니다.");
        }
    }

    /**
     * 실시간 알림 개수 (읽지 않은 알림 수)
     */
    public long countUnreadNotifications(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * 전체 알림 (읽음/안읽음 필터 가능)
     */
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByNotifiedAtDesc(user);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByNotifiedAtDesc(user);
    }

    public List<Notification> getReadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadTrueOrderByNotifiedAtDesc(user);
    }

    /**
     * 페이지네이션 + 검색 필터 처리
     */
    public Page<Notification> getPagedNotifications(User user, String filter, String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        if (keyword != null && !keyword.trim().isEmpty()) {
            return switch (filter) {
                case "unread" -> notificationRepository.findByUserAndIsReadFalseAndMessageContainingIgnoreCaseOrderByNotifiedAtDesc(user, keyword, pageable);
                case "read" -> notificationRepository.findByUserAndIsReadTrueAndMessageContainingIgnoreCaseOrderByNotifiedAtDesc(user, keyword, pageable);
                default -> notificationRepository.findByUserAndMessageContainingIgnoreCaseOrderByNotifiedAtDesc(user, keyword, pageable);
            };
        } else {
            return switch (filter) {
                case "unread" -> notificationRepository.findByUserAndIsReadFalseOrderByNotifiedAtDesc(user, pageable);
                case "read" -> notificationRepository.findByUserAndIsReadTrueOrderByNotifiedAtDesc(user, pageable);
                default -> notificationRepository.findByUserOrderByNotifiedAtDesc(user, pageable);
            };
        }
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification noti = notificationRepository.findById(notificationId).orElseThrow();
        noti.setRead(true);
    }

    /**
     * 알림 전체 읽음 처리
     */
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByNotifiedAtDesc(user);
        for (Notification noti : notifications) {
            noti.setRead(true);
        }
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}