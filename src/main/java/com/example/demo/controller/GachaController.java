package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GachaController {

    // ★ AuthControllerから /gacha の処理を引き継ぎます。
    @GetMapping("/gacha")
    public String index() {
        return "gacha/gacha"; // 修正
    }
<<<<<<< HEAD
    
    // TODO: ガチャ実行ロジック
    @GetMapping("/gacha/roll")
    public String rollGacha(Model model) {
        // 仮の結果表示
        model.addAttribute("gachaResult", "レアなトレーニングアイテム: 腕立て伏せ Lv.3");
        return "gacha_animation";
=======

    // 2. アニメーション画面へ遷移（回数を保持）
    @GetMapping("/gacha/animation")
    public String animation(@RequestParam("count") int count, Model model) {
        // 次の画面（結果取得）に渡すために回数をModelに入れる
        model.addAttribute("count", count);
        return "gacha/gacha_animation"; // 修正
>>>>>>> branch 'master' of https://github.com/Yuya-Hirano-1125/FitWorksApp.git
    }
<<<<<<< HEAD
=======

    // 3. ガチャ結果処理（アニメーション後に呼ばれる）
    @GetMapping("/gacha/roll")
    public String roll(@RequestParam("count") int count, Model model) {
        // Serviceのメソッド名 'roll' を使用し、リストを受け取る
        List<GachaItem> results = gachaService.roll(count);
        
        // 結果画面（result.html）に渡す
        model.addAttribute("results", results);
        
        return "gacha/result"; // 修正
    }
>>>>>>> branch 'master' of https://github.com/Yuya-Hirano-1125/FitWorksApp.git
}


