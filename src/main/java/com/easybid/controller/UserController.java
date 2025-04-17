package com.easybid.controller;

import com.easybid.entity.Bid;
import com.easybid.entity.Payment;
import com.easybid.entity.User;
import com.easybid.service.BidService;
import com.easybid.service.ItemService;
import com.easybid.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BidService bidService;
    private final ItemService itemService;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/login";
    }

    @GetMapping("/mypage")
    public String myPage(@RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "latest") String sort,
                         @RequestParam(defaultValue = "bids") String tab,
                         @RequestParam(required = false, defaultValue = "false") boolean winnerOnly,
                         Model model,
                         Principal principal) {

        String email = principal.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("tab", tab);
        model.addAttribute("winnerOnly", winnerOnly);

        // 사용자의 전체 입찰 내역 가져오기
        List<Bid> allBids = bidService.getMyBids(email, keyword);

        // 🔄 경매 상태 자동 갱신 (경매 종료 여부 판단)
        allBids.forEach(bid -> itemService.updateAuctionStatusIfExpired(bid.getItem()));

        // 정렬 처리
        switch (sort) {
            case "priceDesc" -> allBids.sort(Comparator.comparing(Bid::getBidPrice).reversed());
            case "priceAsc" -> allBids.sort(Comparator.comparing(Bid::getBidPrice));
            default -> allBids.sort(Comparator.comparing(Bid::getBidTime).reversed());
        }

        // 탭 필터링
        List<Bid> filtered = allBids;
        if (tab.equals("winners")) {
            filtered = allBids.stream()
                    .filter(Bid::isWinner)
                    .collect(Collectors.toList());
        } else if (tab.equals("payments")) {
            filtered = allBids.stream()
                    .filter(b -> b.getPaymentStatus() != null && b.getPaymentStatus() == Payment.PaymentStatus.COMPLETED)
                    .collect(Collectors.toList());
        }

        // 낙찰만 보기 필터링
        if (winnerOnly) {
            filtered = filtered.stream()
                    .filter(Bid::isWinner)
                    .collect(Collectors.toList());
        }

        model.addAttribute("myBids", filtered);

        return "mypage";
    }
}
