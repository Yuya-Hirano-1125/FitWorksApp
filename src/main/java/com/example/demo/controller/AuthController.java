package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
 
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // --- スタート画面 ---
    @GetMapping("/")
    public String showStartPage() {
        return "start"; 
    }

    // --- ログイン/登録関連 ---
    @GetMapping("/login")
    public String login() { return "auth/login"; } 

    @GetMapping("/register")
    public String registerForm() { return "auth/register"; } 

    // ★★★ 修正: 実際の登録処理を実装 ★★★
    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model) {
        
        // 1. パスワードの一致確認
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "パスワードと確認用パスワードが一致しません。");
            return "auth/register"; // 入力画面に戻る
        }

        try {
            // 2. サービスを呼び出して保存
            userService.registerNewUser(username, email, password);
            
            // 3. 成功したらログイン画面へリダイレクト（パラメータでメッセージ表示などを制御可能）
            return "redirect:/login?registered"; 
            
        } catch (IllegalArgumentException e) {
            // ユーザー名やメールの重複エラーなど
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            // その他の予期せぬエラー
            e.printStackTrace();
            model.addAttribute("error", "登録中にエラーが発生しました。もう一度お試しください。");
            return "auth/register";
        }
    }

    // --- パスワードリセット ---
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() { return "auth/forgot-password"; } 

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes) {
        boolean emailFoundAndSent = userService.processForgotPassword(email); 
        
        if (emailFoundAndSent) {
            return "redirect:/forgot-password?success"; 
        } else {
            return "redirect:/forgot-password?error"; 
        }
    }

    // --- ホーム画面 ---
    @GetMapping("/home")
    public String home(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
    ) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                model.addAttribute("username", user.getUsername());
                model.addAttribute("level", user.getLevel());
                model.addAttribute("experiencePoints", user.getExperiencePoints());
                model.addAttribute("requiredXp", user.calculateRequiredXp());
                model.addAttribute("progressPercent", user.getProgressPercent());
            } else {
                model.addAttribute("username", userDetails.getUsername());
            }
        } else {
            model.addAttribute("username", "ゲスト");
        }
        return "misc/home"; 
    }
 // --- パスワードリセット実行 (メールリンクから遷移) ---
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        User user = userService.getByResetPasswordToken(token);
        if (user == null) {
            model.addAttribute("error", "無効なリンク、または有効期限切れです。もう一度リセット手続きを行ってください。");
            return "auth/forgot-password"; 
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        
        // 1. トークンの再確認
        User user = userService.getByResetPasswordToken(token);
        if (user == null) {
            model.addAttribute("error", "無効なリンクです。");
            return "auth/forgot-password";
        }

        // 2. パスワード一致確認
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "パスワードが一致しません。");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        // 3. パスワード更新
        userService.updatePassword(user, password);

        return "redirect:/login?resetSuccess";
    }
}