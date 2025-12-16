package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;

@Controller
public class CharactersMenuController {

    @Autowired
    private UserService userService;

    // ホームのキャラクターボタンからキャラクターメニューへ遷移
    @GetMapping("/characters/menu/CharactersMenu")
    public String showCharactersMenu() {
        return "characters/menu/CharactersMenu"; 
    }

    // キャラクター一覧画面へ遷移
    @GetMapping("/characters/menu/CharactersStorage")
    public String showCharactersStorage() {
        return "characters/menu/CharactersStorage"; 
    }

    // キャラクター解放画面へ遷移
    @GetMapping("/characters/menu/CharactersUnlock")
    public String showCharactersUnlock() {
        return "characters/menu/CharactersUnlock"; 
    }

    // --- 背景一覧画面へ遷移 ---
    @GetMapping("/characters/menu/Backgrounds")
    public String showBackgrounds(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        int userLevel = 1; // デフォルト値
        
        try {
            if (userDetails != null) {
                String username = userDetails.getUsername();
                System.out.println("=== DEBUG: 認証ユーザー名: " + username + " ===");
                
                User currentUser = userService.findByUsername(username);
                
                if (currentUser != null) {
                    userLevel = currentUser.getLevel();
                    System.out.println("=== DEBUG: 取得したユーザーレベル: " + userLevel + " ===");
                } else {
                    System.err.println("=== ERROR: ユーザーが見つかりません: " + username + " ===");
                }
            } else {
                System.err.println("=== ERROR: 認証情報がありません ===");
            }
        } catch (Exception e) {
            System.err.println("=== ERROR: ユーザー情報取得エラー: " + e.getMessage() + " ===");
            e.printStackTrace();
        }
        
        model.addAttribute("userLevel", userLevel);
        System.out.println("=== DEBUG: Modelに追加した userLevel: " + userLevel + " ===");
        
        return "characters/menu/Backgrounds"; 
    }
}