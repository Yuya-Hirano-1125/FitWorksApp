package com.example.demo.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.GachaItem;
import com.example.demo.service.GachaService;

@Controller
public class GachaController {

    private final GachaService gachaService;
    private final HttpSession session;

    public GachaController(GachaService gachaService, HttpSession session) {
        this.gachaService = gachaService;
        this.session = session;
    }

    // 1. ガチャトップ画面
    @GetMapping("/gacha")
    public String index(Model model) {

        // ★ ログイン時に保存されている userId を取得
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            // ログインしていない場合はログイン画面へ
            return "redirect:/login";
        }

        model.addAttribute("userId", userId);
        model.addAttribute("probabilityList", gachaService.getProbabilityList());

        return "gacha/gacha";
    }

    // 2. アニメーション画面
    @GetMapping("/gacha/animation")
    public String animation(
            @RequestParam("count") int count,
            @RequestParam("userId") int userId,
            Model model) {

        model.addAttribute("count", count);
        model.addAttribute("userId", userId);

        return "gacha/gacha_animation";
    }

    // 3. ガチャ結果処理
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
