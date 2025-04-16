package com.easybid.repository;

import com.easybid.entity.Payment;
import com.easybid.entity.Item;
import com.easybid.entity.User;
import com.easybid.entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByItemAndWinner(Item item, User winner);

    // 🔍 아이템 ID 기준 결제 존재 여부 확인
    boolean existsByItemId(Long itemId);

    // ✅ 유저별 결제 완료 내역 조회
    List<Payment> findByWinnerAndPaymentStatus(User user, PaymentStatus status);

    // ✅ 유저별 전체 결제 내역 조회
    List<Payment> findByWinner(User user);
}
