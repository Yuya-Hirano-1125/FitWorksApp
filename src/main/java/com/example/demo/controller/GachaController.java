package com.example.demo.controller;

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

    // ガチャトップ画面
    @GetMapping("/gacha")
    public String index(Model model) {
        model.addAttribute("probabilityList", gachaService.getProbabilityList());
        return "gacha/gacha";
    }

    // アニメーション画面へ遷移（回数 & userId）
    @GetMapping("/gacha/animation")
    public String animation(
            @RequestParam("count") int count,
            @RequestParam("userId") int userId,
            Model model) {

        model.addAttribute("count", count);
        model.addAttribute("userId", userId);

        return "gacha/gacha_animation";
    }

    // ガチャ結果処理
    @GetMapping("/gacha/roll")
    public String roll(
            @RequestParam("count") int count,
            @RequestParam("userId") int userId,
            Model model) {

        List<GachaItem> results = gachaService.roll(count, userId);

        model.addAttribute("results", results);
        model.addAttribute("userId", userId);

        return "gacha/result";
    }
}
