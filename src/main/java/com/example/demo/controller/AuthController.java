package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ★ 追加: リダイレクト先にメッセージを渡すために使用

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
        // 登録処理の成功を仮定し、ログイン画面へ遷移 (実際の登録ロジックはUserServiceに依存)
        model.addAttribute("message", "登録が完了しました。ログインしてください。");
        return "login";
    }

    // ----------------------------------------------------
    // ★ 追加機能: パスワード再設定（パスワードを忘れた方）
    // ----------------------------------------------------

    /**
     * パスワード再設定フォーム（メールアドレス入力画面）を表示
     * URL: /forgot-password
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        // Thymeleafテンプレート: forgot-password.html を返します
        return "forgot-password";
    }

    /**
     * パスワード再設定メール送信処理を実行
     * URL: /forgot-password (POST)word);
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, 
                                        RedirectAttributes redirectAttributes) {
        // 【実際の処理】: UserServiceを使ってメールアドレスからユーザーを検索し、
        // リセットトークンを生成してメールを送信するロジックを実装します。

        boolean emailFoundAndSent = true; // ★ 仮の成功フラグ

        if (emailFoundAndSent) {
            // 成功した場合、成功メッセージをリダイレクト先に渡します
            redirectAttributes.addFlashAttribute("successMessage", 
                "パスワードリセット用のリンクをメールアドレス " + email + " 宛に送信しました。");
            return "redirect:/forgot-password";
        } else {
            // 失敗した場合、エラーメッセージをリダイレクト先に渡します
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
            @RequestParam("confirmPassword") String confirmPassword, // 確認用パスワードの取得
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        // 【サーバーサイド検証 1】新しいパスワードと確認用パスワードの一致チェック
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "新しいパスワードが一致しません"); // ★ errorからerrorMessageに修正
            return "change-password";
        }

        // 【サーバーサイド検証 2】 UserServiceによるパスワード変更処理
        // (confirmPassword の取得を修正しました)
        boolean success = userService.changePassword(userDetails.getUsername(), oldPassword, newPassword);

        if(success) {
            model.addAttribute("successMessage", "パスワードが正常に変更されました！🎉"); // ★ messageからsuccessMessageに修正
        } else {
            model.addAttribute("errorMessage", "現在のパスワードが正しくありません"); // ★ errorからerrorMessageに修正
        }
        return "change-password";
    }

    // --- ホーム画面 ---
    @GetMapping("/home")
    public String home() {
        return "home";
    }
}