package com.easybid.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 판매자: users 테이블과의 외래키 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "starting_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal startingPrice;

    @Column(name = "item_description", columnDefinition = "TEXT")
    private String itemDescription;

    @Column(name = "item_image")
    private String itemImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_status", nullable = false)
    private AuctionStatus auctionStatus = AuctionStatus.ACTIVE;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private java.util.List<Bid> bids = new java.util.ArrayList<>();

    // 경매 상태 열거형
    public enum AuctionStatus {
        ACTIVE,
        ENDED
    }
}
