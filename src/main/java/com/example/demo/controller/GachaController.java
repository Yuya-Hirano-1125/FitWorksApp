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

    // ▼ ガチャ画面
    @GetMapping("/gacha")
    public String gacha() {
        return "gacha";
    }

    // ▼ ガチャ演出GIF
    @GetMapping("/gacha/animation")
    public String gachaAnimation(@RequestParam("count") int count, Model model) {
        model.addAttribute("count", count);
        return "gacha_animation";  // GIFページ（animation.html）
    }

    // ▼ ガチャ結果ページ（← これが今回必要！）
    @GetMapping("/gacha/roll")
    public String rollGacha(@RequestParam("count") int count, Model model) {

        // ガチャロジックで結果を生成
        List<GachaItem> results = gachaService.roll(count);

        // HTML（result.html）に渡す
        model.addAttribute("results", results);

        return "result"; // ← 作った gacha結果ページ
    }
}
