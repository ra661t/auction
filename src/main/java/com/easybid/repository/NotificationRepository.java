package com.easybid.repository;

import com.easybid.entity.Notification;
import com.easybid.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 전체 알림 (최신순)
    List<Notification> findByUserOrderByNotifiedAtDesc(User user);

    // 읽지 않은 알림만
    List<Notification> findByUserAndIsReadFalseOrderByNotifiedAtDesc(User user);

    // 읽은 알림만
    List<Notification> findByUserAndIsReadTrueOrderByNotifiedAtDesc(User user);

    // 읽음 상태에 따라 알림 가져오기
    List<Notification> findByUserAndIsReadOrderByNotifiedAtDesc(User user, boolean isRead);

    // 읽지 않은 알림 수 (실시간 알림 배지 표시용)
    long countByUserAndIsReadFalse(User user);

    // 🔍 페이징 + 검색
    Page<Notification> findByUserAndMessageContainingIgnoreCaseOrderByNotifiedAtDesc(User user, String keyword, Pageable pageable);

    Page<Notification> findByUserAndIsReadFalseAndMessageContainingIgnoreCaseOrderByNotifiedAtDesc(User user, String keyword, Pageable pageable);

    Page<Notification> findByUserAndIsReadTrueAndMessageContainingIgnoreCaseOrderByNotifiedAtDesc(User user, String keyword, Pageable pageable);

    Page<Notification> findByUserOrderByNotifiedAtDesc(User user, Pageable pageable);

    // ✅ 새로 추가된 메서드들 (필터링 + 페이징용)
    Page<Notification> findByUserAndIsReadFalseOrderByNotifiedAtDesc(User user, Pageable pageable);

    Page<Notification> findByUserAndIsReadTrueOrderByNotifiedAtDesc(User user, Pageable pageable);

    Page<Notification> findByUserAndIsReadOrderByNotifiedAtDesc(User user, boolean isRead, Pageable pageable);
}