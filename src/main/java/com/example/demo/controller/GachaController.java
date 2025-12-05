package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.User;
import com.example.demo.model.GachaItem;
import com.example.demo.service.GachaService;
import com.example.demo.service.UserService;

@Controller
public class GachaController {

    private final GachaService gachaService;
    private final UserService userService;

    public GachaController(GachaService gachaService, UserService userService) {
        this.gachaService = gachaService;
        this.userService = userService;
    }

    // 1. ガチャ画面
    @GetMapping("/gacha")
    public String index(Model model) {
        model.addAttribute("probabilityList", gachaService.getProbabilityList());
        return "gacha/gacha";
    }

    // 2. 演出画面
    @GetMapping("/gacha/animation")
    public String animation(@RequestParam("count") int count, Model model) {
        model.addAttribute("count", count);
        return "gacha/gacha_animation";
    }

    // 3. ガチャ結果
    @GetMapping("/gacha/roll")
    public String roll(
            @RequestParam("count") int count,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        Long userId = user.getId();

        List<GachaItem> results = gachaService.roll(count, userId);

        model.addAttribute("results", results);

        return "gacha/result";
    }
}
