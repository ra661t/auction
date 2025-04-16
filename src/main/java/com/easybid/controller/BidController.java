package com.easybid.controller;

import com.easybid.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping("/bids/place")
    public String placeBid(@RequestParam Long itemId,
                           @RequestParam BigDecimal bidAmount,
                           Principal principal) {
        bidService.placeBid(itemId, principal.getName(), bidAmount);
        return "redirect:/items/" + itemId;
    }
}
