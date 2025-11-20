package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GachaController {

    // ★ AuthControllerから /gacha の処理を引き継ぎます。
    @GetMapping("/gacha")
    public String index() {
        return "gacha";
    }
    
    // TODO: ガチャ実行ロジック
    @GetMapping("/gacha/roll")
    public String rollGacha(Model model) {
        // 仮の結果表示
        model.addAttribute("gachaResult", "レアなトレーニングアイテム: 腕立て伏せ Lv.3");
        return "gacha_animation";
    }
}



