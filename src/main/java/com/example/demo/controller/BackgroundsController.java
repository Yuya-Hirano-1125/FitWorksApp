package com.example.demo.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.service.UserService;

@Controller
@RequestMapping("/characters") 
public class BackgroundsController {

    @Autowired
    private UserService userService; 

    /**
     * 背景一覧画面を表示するメソッド
     * URL: /characters/backgrounds に対応
     */
    @GetMapping("/backgrounds") 
    public String showBackgrounds(Model model, Principal principal) {
        
        // ログインユーザーが認証されていない場合はログインページにリダイレクト
        if (principal == null) {
            System.err.println("認証情報がありません。");
            return "redirect:/login";
        }

        // 1. ログイン中のユーザー名を取得
        String username = principal.getName();
        
        // 2. ユーザーレベルをサービスから取得
        int userLevel = userService.getUserLevel(username);
        
        // 3. 素材「夢幻の鍵」の所持数を取得
        int dreamKeyCount = userService.getUserMaterialCount(username, "夢幻の鍵");
        
        // ★★★ デバッグ用のコンソール出力 ★★★
        System.out.println("DEBUG: ユーザー名 = " + username);
        System.out.println("DEBUG: 取得されたレベル = " + userLevel);
        System.out.println("DEBUG: 夢幻の鍵の所持数 = " + dreamKeyCount);
        
        // 4. 取得した情報をモデルに追加する
        model.addAttribute("userLevel", userLevel);
        model.addAttribute("dreamKeyCount", dreamKeyCount);
        
        // 5. ビューを返す
        return "characters/Backgrounds";
    }
}