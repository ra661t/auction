package com.easybid.service;

import com.easybid.entity.Bid;
import com.easybid.entity.Item;
import com.easybid.entity.Payment;
import com.easybid.entity.User;
import com.easybid.repository.BidRepository;
import com.easybid.repository.ItemRepository;
import com.easybid.repository.PaymentRepository;
import com.easybid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final BidRepository bidRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 입찰하기
     */
    public void placeBid(Long itemId, String userEmail, BigDecimal bidPrice) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));
        User bidder = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        Optional<Bid> highest = bidRepository.findTopByItemOrderByBidPriceDesc(item);

        if (highest.isPresent()) {
            if (bidPrice.compareTo(highest.get().getBidPrice()) <= 0) {
                throw new IllegalArgumentException("현재 최고 입찰가보다 높은 금액으로 입찰해주세요.");
            }
        } else {
            if (bidPrice.compareTo(item.getStartingPrice()) < 0) {
                throw new IllegalArgumentException("시작가보다 높은 금액으로 입찰해주세요.");
            }
        }

        Bid bid = new Bid();
        bid.setItem(item);
        bid.setBidder(bidder);
        bid.setBidPrice(bidPrice);
        bidRepository.save(bid);
    }

    /**
     * 해당 아이템의 입찰 히스토리
     */
    public List<Bid> getBidHistory(Item item) {
        return bidRepository.findByItemOrderByBidPriceDesc(item);
    }
    public List<Bid> getBidHistory(Long item_id) {
        return bidRepository.findByItemOrderByBidPriceDesc(itemRepository.findById(item_id).orElseThrow(null));
    }

    /**
     * 마이페이지 - 내가 입찰한 목록 (검색 포함, 낙찰/결제 상태 포함)
     */
    public List<Bid> getMyBids(String userEmail, String keyword) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        List<Bid> bids = (keyword != null && !keyword.trim().isEmpty())
                ? bidRepository.searchMyBids(user, keyword.trim())
                : bidRepository.findByBidderOrderByBidTimeDesc(user);

        for (Bid bid : bids) {
            Item item = bid.getItem();
            List<Bid> itemBids = bidRepository.findByItemOrderByBidPriceDesc(item);

            boolean isWinner = isUserHighestBidder(itemBids, user.getEmail());
            bid.setWinner(isWinner);

            if (isWinner && Item.AuctionStatus.ENDED.equals(item.getAuctionStatus())) {
                paymentRepository.findByItemAndWinner(item, user).ifPresentOrElse(
                        payment -> {
                            log.info("[결제 있음] itemId={}, status={}", item.getId(), payment.getPaymentStatus());
                            bid.setPaid(payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED);
                            bid.setPaymentStatus(payment.getPaymentStatus());
                        },
                        () -> {
                            log.info("[결제 없음] itemId={}, userEmail={}", item.getId(), user.getEmail());
                            bid.setPaid(false);
                            bid.setPaymentStatus(Payment.PaymentStatus.PENDING);
                        }
                );
            } else {
                bid.setPaid(false);
                bid.setPaymentStatus(null); // 명확히 null 처리하여 HTML에서 오작동 방지
            }

            bid.setCancelled(false); // 입찰 취소 기능은 아직 없으므로 false 고정
        }

        return bids;
    }

    private boolean isUserHighestBidder(List<Bid> bids, String email) {
        return !bids.isEmpty() && bids.get(0).getBidder().getEmail().equals(email);
    }


    // Page<Item>을 매개변수로 받아서 각 아이템에 대해 가장 높은 입찰을 찾는 메서드
    public Page<Bid> getHighestBidsForItems(Page<Item> itemList, Pageable pageable) {
        List<Bid> highestBids = new ArrayList<>();

        for (Item item : itemList) {
            Bid highestBid = bidRepository.findTopByItemOrderByBidPriceDesc(item).orElseThrow(null);
            if (highestBid != null) {
                highestBids.add(highestBid);
            }
        }

        return new PageImpl<>(highestBids, pageable, highestBids.size());
    }
}