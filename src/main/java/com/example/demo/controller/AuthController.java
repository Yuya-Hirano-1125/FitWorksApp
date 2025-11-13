package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model) {
        // 登録処理の成功を仮定し、ログイン画面へ遷移
        model.addAttribute("message", "登録が完了しました。ログインしてください。");
        return "login";
    }

    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword, // ★修正: confirmPasswordを追加
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        // 【サーバーサイド検証 1】新しいパスワードと確認用パスワードの一致チェック
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "新しいパスワードが一致しません");
            return "change-password";
        }
        
        // 【サーバーサイド検証 2】 UserServiceによるパスワード変更処理
        boolean success = userService.changePassword(userDetails.getUsername(), oldPassword, newPassword);

        if(success) {
            model.addAttribute("message", "パスワードが変更されました");
        } else {
            model.addAttribute("error", "現在のパスワードが正しくありません");
        }
        return "change-password";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}










