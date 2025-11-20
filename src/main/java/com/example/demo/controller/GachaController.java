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
    public String index() {
        return "gacha";
    }

    // 2. アニメーション画面へ遷移（回数を保持）
    @GetMapping("/gacha/animation")
    public String animation(@RequestParam("count") int count, Model model) {
        // 次の画面（結果取得）に渡すために回数をModelに入れる
        model.addAttribute("count", count);
        return "gacha_animation";
    }

    // 3. ガチャ結果処理（アニメーション後に呼ばれる）
    @GetMapping("/gacha/roll")
    public String roll(@RequestParam("count") int count, Model model) {
        // Serviceのメソッド名 'roll' を使用し、リストを受け取る
        List<GachaItem> results = gachaService.roll(count);
        
        // 結果画面（result.html）に渡す
        model.addAttribute("results", results);
        
        return "result";
    }
}













