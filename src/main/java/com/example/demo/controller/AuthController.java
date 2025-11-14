package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.UserService;

@Controller
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    // --- ログイン画面 ---
    @GetMapping("/login")
    public String login() { 
        return "login"; 
    }
    
    // --- 新規登録（GET） ---
    @GetMapping("/register")
    public String registerForm() { 
        return "register"; 
    }
    
    // --- 新規登録（POST） ---
    @PostMapping("/register")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model) {
        
        // 本来は userService.registerUser(username, password);
        model.addAttribute("message", "登録が完了しました。ログインしてください。");
        return "login";
    }
    
    // --- パスワード再設定（ページ表示） ---
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() { 
        return "forgot-password"; 
    }
    
    // --- パスワードリセット処理 ---
    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        
        boolean emailFoundAndSent = true; // 仮のロジック
        
        if (emailFoundAndSent) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "パスワードリセット用リンクを " + email + " に送信しました。"
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "このメールアドレスは登録されていません。"
            );
        }
        return "redirect:/forgot-password";
    }

    // --- パスワード変更（認証後表示） ---
    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "change-password"; 
    }
    
    // --- パスワード変更（認証後処理） ---
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "新しいパスワードが一致しません");
            return "change-password";
        }
        
        boolean success = userService.changePassword(
                userDetails.getUsername(),
                oldPassword,
                newPassword
        );
        
        if (success) {
            model.addAttribute("successMessage", "パスワードが変更されました！");
        } else {
            model.addAttribute("errorMessage", "現在のパスワードが違います");
        }
        
        return "change-password";
    }

    // --- メイン画面と設定画面 ---
    
    @GetMapping("/home")
    public String home() { 
        return "home"; 
    }
    
    @GetMapping("/settings")
    public String settings() { 
        return "settings"; 
    } 
    
    // チャット画面のGETマッピングは ChatController.java に存在すると仮定します
    
    // ★ 設定メニューの遷移先ハンドラー (すべて settings.html に戻るように統一) ★
    
    @GetMapping("/profile/edit")
    public String editProfile() {
        return "settings"; 
    }

    @GetMapping("/settings/notifications")
    public String notificationSettings() {
        // ★ テンプレートエラー回避のため 'settings' に統一
        return "settings"; 
    }

    @GetMapping("/settings/data-privacy")
    public String dataPrivacy() {
        return "settings"; 
    }

    @GetMapping("/subscription")
    public String subscription() {
        return "settings"; 
    }
}