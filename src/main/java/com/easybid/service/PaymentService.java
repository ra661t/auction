package com.easybid.service;

import com.easybid.entity.*;
import com.easybid.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;

    /**
     * 결제 처리
     */
    @Transactional
    public void processPayment(Long itemId, String userEmail) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // 낙찰자인지 확인
        Bid highestBid = bidRepository.findTopByItemOrderByBidPriceDesc(item)
                .orElseThrow(() -> new IllegalArgumentException("입찰 기록이 없습니다."));
        if (!highestBid.getBidder().getId().equals(user.getId())) {
            throw new IllegalStateException("본인이 낙찰자가 아닙니다.");
        }

        // 이미 결제했는지 확인
        if (paymentRepository.findByItemAndWinner(item, user).isPresent()) {
            throw new IllegalStateException("이미 결제 완료된 상품입니다.");
        }

        // 결제 등록
        Payment payment = new Payment();
        payment.setItem(item);
        payment.setWinner(user);
        payment.setFinalPrice(highestBid.getBidPrice());
        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);

        paymentRepository.save(payment);
    }

    /**
     * 낙찰가 조회 (결제폼에 표시용)
     */
    public BigDecimal getFinalBidPrice(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        return bidRepository.findTopByItemOrderByBidPriceDesc(item)
                .map(Bid::getBidPrice)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * ✅ 특정 유저의 결제 완료 내역 조회
     */
    public List<Payment> getCompletedPaymentsByUser(User user) {
        return paymentRepository.findByWinnerAndPaymentStatus(user, Payment.PaymentStatus.COMPLETED);
    }

    /**
     * ✅ 특정 유저의 모든 결제 내역
     */
    public List<Payment> getAllPaymentsByUser(User user) {
        return paymentRepository.findByWinner(user);
    }
}