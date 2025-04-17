package com.easybid.service;

import com.easybid.entity.*;
import com.easybid.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final BidRepository bidRepository;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 전송용

    /**
     * 알림 생성
     */
    public void createNotification(User user, String type, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notificationRepository.save(notification);

        // 실시간 알림 전송
        NotificationMessage payload = new NotificationMessage(type, message);
        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", payload);
    }

    /**
     * 경매 종료 후 알림 처리 (판매자, 낙찰자, 입찰자 모두에게 발송)
     */
    @Transactional
    public void notifyAuctionResult(User seller, String itemName, User winner, Item item) {
        Set<User> recipients = new HashSet<>();

        // 판매자 추가
        recipients.add(seller);

        // 낙찰자 추가
        if (winner != null) {
            recipients.add(winner);
        }

        // 입찰자 전체 추가
        List<User> bidders = bidRepository.findDistinctBiddersByItem(item);
        recipients.addAll(bidders);

        for (User user : recipients) {
            if (user.equals(seller)) {
                if (winner == null) {
                    createNotification(user, "판매 실패", "상품 [" + itemName + "] 이(가) 낙찰되지 않았습니다.");
                } else {
                    createNotification(user, "판매 완료", "상품 [" + itemName + "] 이(가) " + winner.getName() + "님에게 낙찰되었습니다.");
                }
            } else if (user.equals(winner)) {
                createNotification(user, "낙찰 성공", "축하합니다! 상품 [" + itemName + "] 을(를) 낙찰받았습니다.");
            } else {
                createNotification(user, "낙찰 실패", "입찰하신 상품 [" + itemName + "] 에서 낙찰되지 못했습니다.");
            }
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
