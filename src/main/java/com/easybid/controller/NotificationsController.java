package com.easybid.controller;

import com.easybid.entity.Notification;
import com.easybid.entity.User;
import com.easybid.entity.Bid;
import com.easybid.entity.Item;
import com.easybid.service.NotificationService;
import com.easybid.service.UserService;
import com.easybid.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationsController {

    private final NotificationService notificationService;
    private final UserService userService;
    private final BidRepository bidRepository;

    /**
     * 🔔 알림 목록 페이지 (전체, 읽음, 안읽음 필터링 포함 + 검색 + 페이지네이션)
     */
    @GetMapping
    public String listNotifications(Model model,
                                    Principal principal,
                                    @RequestParam(defaultValue = "all") String filter,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "0") int page) {
        String email = principal.getName();
        User user = userService.findByEmail(email);

        Page<Notification> notifications = notificationService.getPagedNotifications(user, filter, keyword, page);
        model.addAttribute("notifications", notifications);
        model.addAttribute("filter", filter);
        model.addAttribute("keyword", keyword);

        return "notifications/list"; // templates/notifications/list.html
    }

    /**
     * ✅ 알림 1건 읽음 처리
     */
    @PostMapping("/read")
    public String markAsRead(@RequestParam Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    /**
     * 🔁 전체 읽음 처리
     */
    @PostMapping("/readAll")
    public String markAllAsRead(Principal principal) {
        String email = principal.getName();
        User user = userService.findByEmail(email);
        notificationService.markAllAsRead(user);
        return "redirect:/notifications";
    }

    /**
     * ❌ 알림 삭제
     */
    @PostMapping("/delete")
    public String deleteNotification(@RequestParam Long id) {
        notificationService.deleteNotification(id);
        return "redirect:/notifications";
    }

    /**
     * 📊 실시간 읽지 않은 알림 개수 (AJAX)
     */
    @GetMapping("/unreadCount")
    @ResponseBody
    public ResponseEntity<Integer> getUnreadCount(Principal principal) {
        String email = principal.getName();
        User user = userService.findByEmail(email);
        long count = notificationService.countUnreadNotifications(user);
        return ResponseEntity.ok((int) count);
    }

    /**
     * ⏰ 경매 종료 10분 전 알림 자동 전송 (1분마다 실행)
     */
    @Scheduled(fixedRate = 60000)
    public void notifyBiddersBeforeAuctionEnds() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTimeStart = now.plusMinutes(10);
        LocalDateTime targetTimeEnd = now.plusMinutes(11);

        List<Bid> bids = bidRepository.findDistinctByItem_EndTimeBetweenAndItem_AuctionStatus(
                targetTimeStart, targetTimeEnd, Item.AuctionStatus.ACTIVE);

        for (Bid bid : bids) {
            User bidder = bid.getBidder();
            Item item = bid.getItem();
            notificationService.createNotification(
                    bidder,
                    "입찰 마감 임박",
                    "[" + item.getItemName() + "] 경매가 10분 후 종료됩니다. 마지막 입찰 기회를 놓치지 마세요!"
            );
        }
    }
}