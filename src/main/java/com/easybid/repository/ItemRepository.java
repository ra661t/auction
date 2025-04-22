package com.easybid.repository;

import com.easybid.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    // 기본적인 CRUD는 JpaRepository로 충분합니다.
    List<Item> findTop6ByAuctionStatusAndEndTimeAfterOrderByEndTimeAsc(Item.AuctionStatus status, LocalDateTime now);
    Page<Item> findByItemNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Item> findByAuctionStatus(Item.AuctionStatus status, Pageable pageable);
    Page<Item> findByItemNameContainingIgnoreCaseAndAuctionStatus(String keyword, Item.AuctionStatus status, Pageable pageable);
    @Query("SELECT i FROM Item i LEFT JOIN i.bids b GROUP BY i.id ORDER BY COUNT(b.id) DESC")
    List<Item> findTopBiddedItems(Pageable pageable);

}
