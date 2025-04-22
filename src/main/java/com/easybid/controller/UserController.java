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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // ✅ 1. 내 정보 페이지 (프로필)
    @GetMapping("/mypage/profile")
    public String getProfilePage(Model model, Principal principal) {
        String email = principal.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);
        return "mypage-profile"; // ← 템플릿 파일명
    }

    // ✅ 2. 입찰/낙찰/결제 내역 페이지
    @GetMapping("/mypage/bid-history")
    public String getBidHistoryPage(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "latest") String sort,
                                    @RequestParam(defaultValue = "false") boolean winnerOnly,
                                    Model model,
                                    Principal principal) {

        String email = principal.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("winnerOnly", winnerOnly);

        // 입찰 내역
        List<Bid> allBids = bidService.getMyBids(email, keyword);
        allBids.forEach(bid -> itemService.updateAuctionStatusIfExpired(bid.getItem()));

        // 정렬
        switch (sort) {
            case "priceDesc" -> allBids.sort(Comparator.comparing(Bid::getBidPrice).reversed());
            case "priceAsc" -> allBids.sort(Comparator.comparing(Bid::getBidPrice));
            default -> allBids.sort(Comparator.comparing(Bid::getBidTime).reversed());
        }

        // 낙찰/결제 여부 필터링
        List<Bid> filtered = allBids.stream()
                .filter(bid -> !winnerOnly || bid.isWinner())
                .collect(Collectors.toList());

        model.addAttribute("myBids", filtered);
        return "mypage-bid-history"; // ← 템플릿 파일명
    }
    // GET: 수정 폼 보기
    @GetMapping("/mypage/profile/edit")
    public String editProfileForm(Model model, Principal principal) {
        String email = principal.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);
        return "mypage-profile-edit";
    }

    // POST: 수정 처리
    @PostMapping("/mypage/profile/edit")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String password,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage/profile/edit";
        }

        boolean success = userService.changePassword(principal.getName(), currentPassword, password);
        if (!success) {
            redirectAttributes.addFlashAttribute("error", "현재 비밀번호가 올바르지 않습니다.");
            return "redirect:/mypage/profile/edit";
        }

        return "redirect:/mypage/profile";
    }




}
