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
        this.userService = userService; // 初期化済み
    }

    // --- ログイン/登録関連 ---
    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm() { return "register"; }

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        model.addAttribute("message", "登録が完了しました。ログインしてください。");
        return "login";
    }

    // --- パスワードリセット ---
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() { return "forgot-password"; }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes) {
        boolean emailFoundAndSent = true; 
        if (emailFoundAndSent) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "パスワードリセット用のリンクをメールアドレス " + email + " 宛に送信しました。");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "そのメールアドレスは登録されていません。");
        }
        return "redirect:/forgot-password";
    }

    // --- パスワード変更 ---
    @GetMapping("/change-password")
    public String changePasswordForm() { return "change-password"; }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String oldPassword,
                               @RequestParam("newPassword") String newPassword,
                               @RequestParam("confirmPassword") String confirmPassword,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "新しいパスワードが一致しません");
            return "change-password";
        }
        boolean success = true; // 仮の成功フラグ
        if(success) {
            model.addAttribute("successMessage", "パスワードが正常に変更されました！🎉");
        } else {
            model.addAttribute("errorMessage", "現在のパスワードが正しくありません");
        }
        return "change-password";
    }

    // --- ホーム画面 (ユーザー名反映) ---
    @GetMapping("/home")
    public String home(
        @AuthenticationPrincipal UserDetails userDetails, 
        Model model 
    ) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        } else {
            model.addAttribute("username", "ゲスト");
        }
        return "home";
    }

    // --- 設定画面遷移 ---
    @GetMapping("/settings")
    public String settings() { return "settings"; }
}