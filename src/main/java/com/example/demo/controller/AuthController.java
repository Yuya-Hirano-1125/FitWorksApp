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

    // --- 新規登録画面（GET） ---
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    // --- 新規登録処理（POST） ---
    @PostMapping("/register")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model) {
        // 登録処理の成功を仮定し、ログイン画面へ遷移
        model.addAttribute("message", "登録が完了しました。ログインしてください。");
        return "login";
    }

    // ----------------------------------------------------
    // ★ パスワード再設定（パスワードを忘れた方）
    // ----------------------------------------------------

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes) {
        // ★ 仮の処理ロジック (成功を仮定)
        boolean emailFoundAndSent = true; 
        
        if (emailFoundAndSent) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "パスワードリセット用のリンクをメールアドレス " + email + " 宛に送信しました。");
            return "redirect:/forgot-password";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "そのメールアドレスは登録されていません。");
            return "redirect:/forgot-password";
        }
    }

    // ----------------------------------------------------
    // --- 認証後のパスワード変更（現在のパスワードが必要） ---
    // ----------------------------------------------------
    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        // 新しいパスワードと確認用パスワードの一致チェック
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "新しいパスワードが一致しません");
            return "change-password";
        }

        // UserServiceによるパスワード変更処理を実行 (成功/失敗)
        // boolean success = userService.changePassword(userDetails.getUsername(), oldPassword, newPassword);
        boolean success = true; // ★ 仮の成功フラグ
        
        if(success) {
            model.addAttribute("successMessage", "パスワードが正常に変更されました！🎉");
        } else {
            model.addAttribute("errorMessage", "現在のパスワードが正しくありません");
        }
        return "change-password";
    }

    // --- ホーム画面 (ユーザー名表示機能を追加) ---
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

    // ----------------------------------------------------
    // ★ 設定画面 (新規追加)
    // ----------------------------------------------------
    @GetMapping("/settings")
    public String settings() {
        return "settings"; // settings.html を返します
    }
}


















