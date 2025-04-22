package com.easybid.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    //@JsonIgnoreProperties({"bids"})
    @JsonIgnore
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    @JsonIgnoreProperties({"id", "email", "password", "phone", "imageUrl", "hibernateLazyInitializer", "handler"})
    //@JsonIgnore
    private User bidder;

    @Column(name = "bid_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal bidPrice;

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime = LocalDateTime.now();

    // ✅ 낙찰 여부 (DB 비저장)
    @Transient
    private boolean winner;

    // ✅ 결제 여부 (DB 비저장)
    @Transient
    private boolean paid;

    // ✅ 결제 상태 (예: PENDING, COMPLETED, FAILED)
    @Transient
    private Payment.PaymentStatus paymentStatus;

    // ✅ 입찰 취소 여부 (DB 비저장)
    @Transient
    private boolean cancelled;
}