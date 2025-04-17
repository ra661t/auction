package com.easybid.service;

import com.easybid.entity.Bid;
import com.easybid.entity.Item;
import com.easybid.entity.User;
import com.easybid.repository.BidRepository;
import com.easybid.repository.ItemRepository;
import com.easybid.repository.PaymentRepository;
import com.easybid.repository.UserRepository;
import com.easybid.service.NotificationService;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    // 경매 상품 등록
    @Transactional
    public void createItem(Item item, String userEmail) {
        User seller = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        item.setSeller(seller);
        itemRepository.save(item);
    }

    // 상품 상세 조회 (종료 여부 확인 포함)
    public Optional<Item> getItem(Long id) {
        Optional<Item> optionalItem = itemRepository.findById(id);
        optionalItem.ifPresent(this::updateAuctionStatusIfExpired);
        return optionalItem;
    }

    // 전체 상품 목록 (페이징, 종료 여부 확인 포함)
    public Page<Item> getItemList(Pageable pageable) {
        Page<Item> items = itemRepository.findAll(pageable);
        items.forEach(this::updateAuctionStatusIfExpired);
        return items;
    }

    // 상품 수정
    @Transactional
    public void updateItem(Long id, Item updatedItem) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        item.setItemName(updatedItem.getItemName());
        item.setStartingPrice(updatedItem.getStartingPrice());
        item.setItemDescription(updatedItem.getItemDescription());
        item.setItemImage(updatedItem.getItemImage());
        item.setEndTime(updatedItem.getEndTime());
        item.setAuctionStatus(updatedItem.getAuctionStatus());
    }

    // 상품 삭제
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    // ✅ 최고 입찰가 조회
    public Optional<Bid> findHighestBidByItem(Item item) {
        return bidRepository.findTopByItemOrderByBidPriceDesc(item);
    }

    // ✅ 결제 여부 확인
    public boolean isPaymentExists(Long itemId) {
        return paymentRepository.existsByItemId(itemId);
    }

    // ✅ 경매 종료 시간 도달 시 상태 자동 변경 + 알림 처리
    @Transactional
    public void updateAuctionStatusIfExpired(Item item) {
        if (item.getAuctionStatus() == Item.AuctionStatus.ACTIVE &&
                item.getEndTime().isBefore(LocalDateTime.now())) {
            item.setAuctionStatus(Item.AuctionStatus.ENDED);
            itemRepository.save(item);

            // 낙찰자 조회
            Optional<Bid> highestBid = bidRepository.findTopByItemOrderByBidPriceDesc(item);
            User winner = highestBid.map(Bid::getBidder).orElse(null);

            // 알림 전송
            notificationService.notifyAuctionResult(item.getSeller(), item.getItemName(), winner);
        }
    }
    // ✅ 홈 화면용 - 종료되지 않은 경매 중 종료 시간이 가까운 6개 조회
    public List<Item> getTop6ActiveItems() {
        return itemRepository.findTop6ByAuctionStatusAndEndTimeAfterOrderByEndTimeAsc(
                Item.AuctionStatus.ACTIVE, LocalDateTime.now());
    }
    public Page<Item> searchItemsByName(String keyword, Pageable pageable) {
        Page<Item> items = itemRepository.findByItemNameContainingIgnoreCase(keyword, pageable);
        items.forEach(this::updateAuctionStatusIfExpired); // 상태 자동 갱신
        return items;
    }
    public Page<Item> searchItemsByStatus(String status, Pageable pageable) {
        Item.AuctionStatus auctionStatus = Item.AuctionStatus.valueOf(status);
        Page<Item> items = itemRepository.findByAuctionStatus(auctionStatus, pageable);
        items.forEach(this::updateAuctionStatusIfExpired);
        return items;
    }

    public Page<Item> searchItemsByNameAndStatus(String keyword, String status, Pageable pageable) {
        Item.AuctionStatus auctionStatus = Item.AuctionStatus.valueOf(status);
        Page<Item> items = itemRepository.findByItemNameContainingIgnoreCaseAndAuctionStatus(keyword, auctionStatus, pageable);
        items.forEach(this::updateAuctionStatusIfExpired);
        return items;
    }

}
