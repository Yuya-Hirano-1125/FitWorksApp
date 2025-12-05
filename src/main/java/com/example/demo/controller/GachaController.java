package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.GachaItem;
import com.example.demo.service.GachaService;

@Controller
public class GachaController {

    private final GachaService gachaService;

    public GachaController(GachaService gachaService) {
        this.gachaService = gachaService;
    }

    // --- ガチャトップ ---
    @GetMapping("/gacha")
    public String index(
            @RequestParam("userId") Integer userId,
            Model model) {

        if (userId == null) {
            return "redirect:/login"; // 念のため
        }

        model.addAttribute("userId", userId);
        model.addAttribute("probabilityList", gachaService.getProbabilityList());

        return "gacha/gacha";
    }

    // --- ガチャ演出ページ ---
    @GetMapping("/gacha/animation")
    public String animation(
            @RequestParam("userId") Integer userId,
            @RequestParam("count") int count,
            Model model) {

        if (userId == null) return "redirect:/login";

        model.addAttribute("userId", userId);
        model.addAttribute("count", count);

        return "gacha/gacha_animation";
    }

    // --- ガチャ結果 ---
    @GetMapping("/gacha/roll")
    public String roll(
            @RequestParam("userId") Integer userId,
            @RequestParam("count") int count,
            Model model) {

        if (userId == null) return "redirect:/login";

        List<GachaItem> results = new ArrayList<>();

        // 1回ずつ DB 保存付きのガチャを回す
        for (int i = 0; i < count; i++) {
            results.add(gachaService.drawGacha(userId));
        }

        model.addAttribute("results", results);
        model.addAttribute("userId", userId);

        return "gacha/result";
    }
}
