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
import com.example.demo.repository.ItemRepository; // 追加
import com.example.demo.repository.UserItemRepository; // 追加
import com.example.demo.service.GachaService;
import com.example.demo.service.UserService;

@Controller
public class GachaController {

    private final GachaService gachaService;
    private final UserService userService;
    private final UserItemRepository userItemRepository; // 追加
    private final ItemRepository itemRepository; // 追加

    // コンストラクタ注入に追加
    public GachaController(GachaService gachaService, UserService userService, 
                           UserItemRepository userItemRepository, ItemRepository itemRepository) {
        this.gachaService = gachaService;
        this.userService = userService;
        this.userItemRepository = userItemRepository;
        this.itemRepository = itemRepository;
    }

 // 1. ガチャ画面
    @GetMapping("/gacha")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // ログインユーザー情報の取得
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            Long userId = user.getId();

            model.addAttribute("userId", userId); // HTMLでのリンク生成に使用

            // ★修正: usersテーブルのchipカラムを直接参照
            int chipCount = user.getChipCount();  // Userエンティティのgetter
            model.addAttribute("chipCount", chipCount);

        } else {
            // 未ログイン時は0を表示
            model.addAttribute("chipCount", 0);
            model.addAttribute("userId", 0L);
        }

        model.addAttribute("probabilityList", gachaService.getProbabilityList());
        return "gacha/gacha";
    }

    // 2. 演出画面
    @GetMapping("/gacha/animation")
    public String animation(@RequestParam("count") int count, @RequestParam("userId") Long userId, Model model) {
        model.addAttribute("count", count);
        model.addAttribute("userId", userId); // 結果画面へ遷移するために必要なら渡す
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

        // ここでシェイカー消費ロジックが必要な場合はServiceに実装する
        // gachaService.consumeShaker(userId, count); 

        List<GachaItem> results = gachaService.roll(count, userId);

        model.addAttribute("results", results);

        return "gacha/result";
    }
}