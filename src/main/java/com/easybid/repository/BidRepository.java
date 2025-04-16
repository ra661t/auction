package com.easybid.repository;

import com.easybid.entity.Bid;
import com.easybid.entity.Item;
import com.easybid.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByItemOrderByBidPriceDesc(Item item); // 입찰가 높은 순

    Optional<Bid> findTopByItemOrderByBidPriceDesc(Item item); // 최고가 하나만

    // ✅ 특정 유저가 입찰한 목록
    List<Bid> findByBidderOrderByBidTimeDesc(User bidder);

    // ✅ 특정 유저의 입찰 중 상품명 키워드 검색
    @Query("SELECT b FROM Bid b WHERE b.bidder = :user AND b.item.itemName LIKE %:keyword% ORDER BY b.bidTime DESC")
    List<Bid> searchMyBids(@Param("user") User user, @Param("keyword") String keyword);
}
