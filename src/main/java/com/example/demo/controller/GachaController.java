package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.User;
import com.example.demo.model.GachaItem;
import com.example.demo.service.GachaService;
import com.example.demo.service.UserService;

@Controller
public class GachaController {

    private final GachaService gachaService;
    private final UserService userService;
    // 削除: UserItemRepository, ItemRepository (未使用のため)

    // コンストラクタからもリポジトリを削除
    public GachaController(GachaService gachaService, UserService userService) {
        this.gachaService = gachaService;
        this.userService = userService;
    }

    // 1. ガチャ画面
    @GetMapping("/gacha")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            Long userId = user.getId();

            model.addAttribute("userId", userId);

            // 変数名を coinCount に変更
            int coinCount = user.getChipCount();
            model.addAttribute("coinCount", coinCount);

        } else {
            model.addAttribute("coinCount", 0);
            model.addAttribute("userId", 0L);
        }

        model.addAttribute("probabilityList", gachaService.getProbabilityList());
        return "gacha/gacha";
    }

    // 2. 演出画面
    @PostMapping("/gacha/animation")
    public String animation(@RequestParam("count") int count, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        // userIdはURLで受け取らず、認証情報があるかだけのチェック（必要なら）に使用
        // 演出画面には count だけ渡せばOK
        model.addAttribute("count", count);
        return "gacha/gacha_animation";
    }

    // 3. ガチャ抽選処理
    @PostMapping("/gacha/draw") 
    public String draw(@RequestParam("count") int count,
                       @AuthenticationPrincipal UserDetails userDetails,
                       RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        Long userId = user.getId(); // ここでサーバー側でIDを取得

        // コイン消費ロジック
        int cost = (count == 1) ? 1 : 10;
        boolean success = user.useChips(cost);

        if (!success) {
            redirectAttributes.addFlashAttribute("errorMessage", "コインが不足しています！");
            return "redirect:/gacha";
        }

        userService.save(user);

        // ★注意: GachaService内でも UserItemRepository を使用している場合、
        // そこも user.addItem(...) を使う形に修正が必要になります。
        List<GachaItem> results = gachaService.roll(count, userId);
        
        redirectAttributes.addFlashAttribute("results", results);
        redirectAttributes.addFlashAttribute("coinCount", user.getChipCount());
        redirectAttributes.addFlashAttribute("userId", userId);

        return "redirect:/gacha/result";
    }

    // 4. ガチャ結果表示
    @GetMapping("/gacha/result")
    public String result(Model model) {
        if (!model.containsAttribute("results")) {
            return "redirect:/gacha";
        }
        return "gacha/result";
    }
}