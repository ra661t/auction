package com.easybid.controller;

import com.easybid.entity.Bid;
import com.easybid.entity.Item;
import com.easybid.service.BidService;
import com.easybid.service.ItemService;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final BidService bidService;

    // 전체 경매 목록
    @GetMapping("/list")
    public String getAllItems(
                                @RequestParam(name = "keyword", required = false) String keyword,
                                @RequestParam(name = "sort", required = false) String sort,
                                @RequestParam(name = "status", required = false) String status,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                @RequestParam(name = "size", defaultValue = "10") int size,
                                Model model) {

        // ✅ 기본 상태값: "ACTIVE"
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }

        // ✅ 정렬 파라미터 처리
        Sort sortOption = Sort.by("id").descending(); // 기본값
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            if (parts.length == 2) {
                sortOption = "asc".equalsIgnoreCase(parts[1])
                        ? Sort.by(parts[0]).ascending()
                        : Sort.by(parts[0]).descending();
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortOption);
        Page<Item> itemList;

        // ✅ 필터 조합 처리
        if (keyword != null && !keyword.isBlank()) {
            if ("ALL".equalsIgnoreCase(status)) {
                itemList = itemService.searchItemsByName(keyword, pageable);
            } else {
                itemList = itemService.searchItemsByNameAndStatus(keyword, status, pageable);
            }
        } else {
            if ("ALL".equalsIgnoreCase(status)) {
                itemList = itemService.getItemList(pageable);
            } else {
                itemList = itemService.searchItemsByStatus(status, pageable);
            }
        }

        model.addAttribute("itemList", itemList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("status", status);


        // Page<Bid> highestBidList;
        // highestBidList = bidService.getHighestBidsForItems(itemList, pageable);
        // model.addAttribute("highestBids", highestBidList);
        return "item/list";
    }


    // 경매 등록 폼
    @GetMapping("/new")
    public String createItemForm(Model model) {
        model.addAttribute("item", new Item());
        return "item/new";
    }

    // 경매 등록 완료
    @PostMapping("/upload")
    public String uploadItem(
        @RequestParam("itemName") String itemName,
        @RequestParam("startingPrice") BigDecimal startingPrice,
        @RequestParam("itemDescription") String itemDescription,
        @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @RequestParam("itemImage") MultipartFile itemImage,
        Principal principal, Model model
    ) throws IOException {

        Item item = new Item();
        item.setItemName(itemName);
        item.setStartingPrice(startingPrice);
        item.setItemDescription(itemDescription);
        item.setEndTime(endTime);

        if (!itemImage.isEmpty()) {
            item.setItemImage(itemImage.getBytes());
        }

        itemService.createItem(item, principal.getName());


        model.addAttribute("message", "경매 상품이 등록되었습니다.");
        model.addAttribute("redirectUrl", "/items/list");
        return "item/alert";
    }
    // public String createItem(@ModelAttribute Item item, Principal principal, Model model) {
    //     itemService.createItem(item, principal.getName());

    //     model.addAttribute("message", "경매 상품이 등록되었습니다.");
    //     model.addAttribute("redirectUrl", "/items/list");
    //     return "item/alert";
    // }

    // 이미지 반환
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long id) {
        Optional<Item> itemOpt = itemService.getItem(id);

        if (itemOpt.isPresent() && itemOpt.get().getItemImage() != null) {
            byte[] imageBytes = itemOpt.get().getItemImage();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }

    // 경매 상세보기
    @GetMapping("/{id}")
    public String getItem(@PathVariable("id") Long id, Model model, Principal principal) {
        Item item = itemService.getItem(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        // 🔄 종료 상태 자동 업데이트 + 알림
        itemService.updateAuctionStatusIfExpired(item);

        String userEmail = principal.getName();
        model.addAttribute("item", item);
        model.addAttribute("currentUserEmail", userEmail);

        // 최고 입찰가 조회
        Optional<Bid> highestBid = itemService.findHighestBidByItem(item);
        BigDecimal highestBidPrice = highestBid.map(Bid::getBidPrice).orElse(null);
        model.addAttribute("highestBidPrice", highestBidPrice);

        // 낙찰자 여부 판단
        boolean isWinner = highestBid.map(bid -> bid.getBidder().getEmail().equals(userEmail)).orElse(false);
        model.addAttribute("isWinner", isWinner);

        // 결제 여부 판단
        boolean notPaid = !itemService.isPaymentExists(item.getId());
        model.addAttribute("notPaid", notPaid);

        return "item/detail";
    }

    // 경매 수정 폼
    @GetMapping("/{id}/edit")
    public String editItemForm(@PathVariable("id") Long id, Model model, Principal principal) {
        Item item = itemService.getItem(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!item.getSeller().getEmail().equals(principal.getName())) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        model.addAttribute("item", item);
        return "item/edit";
    }

    // 경매 수정 완료
    @PostMapping("/{id}/edit")
    public String updateItem(@PathVariable("id") Long id, @ModelAttribute Item item, Principal principal, Model model) {
        Item originalItem = itemService.getItem(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!originalItem.getSeller().getEmail().equals(principal.getName())) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        itemService.updateItem(id, item);

        model.addAttribute("message", "경매 상품이 수정되었습니다.");
        model.addAttribute("redirectUrl", "/items/list");
        return "item/alert";
    }

    // 경매 삭제
    @PostMapping("/{id}/delete")
    public String deleteItem(@PathVariable("id") Long id, Principal principal, Model model) {
        Item item = itemService.getItem(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!item.getSeller().getEmail().equals(principal.getName())) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }

        itemService.deleteItem(id);

        model.addAttribute("message", "경매 상품이 삭제되었습니다.");
        model.addAttribute("redirectUrl", "/items/list");
        return "item/alert";
    }
}
