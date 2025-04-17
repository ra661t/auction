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
        List<Item> itemList = itemService.getTop6ActiveItems();
        model.addAttribute("itemList", itemList);
        return "home";
    }
}
