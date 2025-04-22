package com.easybid.repository;

import com.easybid.entity.Bid;
import com.easybid.entity.Item;
import com.easybid.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByItemOrderByBidPriceDesc(Item item); // 입찰가 높은 순

    Optional<Bid> findTopByItemOrderByBidPriceDesc(Item item); // 최고가 하나만

    @Query("SELECT b FROM Bid b WHERE b.item = :item AND b.bidPrice = (SELECT MAX(b2.bidPrice) FROM Bid b2 WHERE b2.item = :item)")
    Page<Bid> findHighestBidForItem(@Param("item") Item item, Pageable pageable);

    // ✅ 특정 유저가 입찰한 목록
    List<Bid> findByBidderOrderByBidTimeDesc(User bidder);

    // ✅ 특정 유저의 입찰 중 상품명 키워드 검색
    @Query("SELECT b FROM Bid b WHERE b.bidder = :user AND b.item.itemName LIKE %:keyword% ORDER BY b.bidTime DESC")
    List<Bid> searchMyBids(@Param("user") User user, @Param("keyword") String keyword);

    // ✅ 특정 상품에 입찰한 모든 사용자 조회 (중복 제거)
    @Query("SELECT DISTINCT b.bidder FROM Bid b WHERE b.item = :item")
    List<User> findDistinctBiddersByItem(@Param("item") Item item);

    // ✅ 경매 종료 10분 전 대상 입찰자 조회 (알림 전용)
    @Query("SELECT DISTINCT b FROM Bid b WHERE b.item.endTime BETWEEN :start AND :end AND b.item.auctionStatus = :status")
    List<Bid> findDistinctByItem_EndTimeBetweenAndItem_AuctionStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") Item.AuctionStatus status);
}