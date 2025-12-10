package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/ranking")
public class RankingController {

    private final UserService userService;

    public RankingController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String index(Model model, Principal principal) {
        String username = principal.getName();
        // フレンド内ランキングを取得
        List<User> rankingList = userService.getFriendRanking(username);
        
        model.addAttribute("rankingList", rankingList);
        return "misc/ranking"; 
    }
}