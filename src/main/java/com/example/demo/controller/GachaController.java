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

    // 1. ガチャトップ画面
    @GetMapping("/gacha")
    public String index(Model model) {
        model.addAttribute("probabilityList", gachaService.getProbabilityList());
        return "gacha/gacha"; 
    }

    // 2. アニメーション画面へ遷移（回数 & userId を保持）
    @GetMapping("/gacha/animation")
    public String animation(
            @RequestParam("count") int count,
            @RequestParam("userId") String userId,
            Model model) {

        model.addAttribute("count", count);
        model.addAttribute("userId", userId);  // ★追加

        return "gacha/gacha_animation"; 
    }

    // 3. ガチャ結果処理（アニメーション後に呼ばれる）
    @GetMapping("/gacha/roll")
    public String roll(
            @RequestParam("count") int count,
            @RequestParam("userId") String userId,  // ★追加
            Model model) {

        // Service に userId を渡す（DB 保存される）
        List<GachaItem> results = gachaService.roll(count, userId);

        model.addAttribute("results", results);
        model.addAttribute("userId", userId); // ★保持して次画面でも使える

        return "gacha/result"; 
    }
}
