package com.easybid.controller;

import com.easybid.entity.Item;
import com.easybid.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final ItemService itemService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Item> topBiddedItems = itemService.getTopBiddedItems(3); // 입찰 많은 순
        model.addAttribute("topBiddedItems", topBiddedItems);
        return "home";
    }
}
