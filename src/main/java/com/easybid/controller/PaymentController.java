package com.easybid.controller;

import com.easybid.entity.Item;
import com.easybid.service.ItemService;
import com.easybid.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ItemService itemService;

    // 결제 처리
    @PostMapping("/payments/pay")
    public String pay(@RequestParam Long itemId, Principal principal) {
        paymentService.processPayment(itemId, principal.getName());
        return "redirect:/items/" + itemId;
    }

    // 결제 폼 페이지 이동
    @GetMapping("/payments/form")
    public String paymentForm(@RequestParam Long itemId, Principal principal, Model model) {
        Item item = itemService.getItem(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        BigDecimal finalPrice = paymentService.getFinalBidPrice(itemId);

        model.addAttribute("item", item);
        model.addAttribute("finalPrice", finalPrice);

        return "payments/form"; // templates/payments/form.html
    }
}
