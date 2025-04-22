package com.easybid.controller;

import com.easybid.entity.Bid;
import com.easybid.service.BidService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    // public String placeBid(@RequestParam Long itemId,
    //                        @RequestParam BigDecimal bidAmount,
    //                        Principal principal) {
    //     bidService.placeBid(itemId, principal.getName(), bidAmount);
    //     return "redirect:/items/" + itemId;
    // }

    //public ResponseEntity<?> placeBid(@RequestBody Map<String, Object> payload, Principal principal) {

    @PostMapping("/place")
    public String placeBid(
        @RequestParam("itemId") Long itemId,
        @RequestParam("bidAmount") BigDecimal bidAmount, Principal principal) {

        //Long itemId = Long.valueOf(payload.get("itemId").toString());
        //BigDecimal bidAmount = BigDecimal.valueOf(Integer.parseInt(payload.get("bidPrice").toString()));
        
        bidService.placeBid(itemId, principal.getName(), bidAmount);

        //return ResponseEntity.ok("입찰 완료");
        return "redirect:/items/" + itemId;
    }

    // 입찰 목록 조회
    @GetMapping("/{itemId}")
    @ResponseBody
    public List<Bid> getBids(@PathVariable("itemId") Long itemId) {
        List<Bid> bidList = bidService.getBidHistory(itemId);
        return bidList;
    }
}
