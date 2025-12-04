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

    @GetMapping("/")
    public String showStartPage() { return "start"; }

    @GetMapping("/login")
    public String login() { return "auth/login"; } 

    // --- 新規登録 ---
    @GetMapping("/register")
    public String registerForm() { return "auth/register"; } 

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("email") String email,
                               @RequestParam("phoneNumber") String phoneNumber, // 追加
                               @RequestParam("password") String password,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "パスワードが一致しません。");
            return "auth/register";
        }
        try {
            // 電話番号も含めて登録
            userService.registerNewUser(username, email, phoneNumber, password);
            return "redirect:/login?registered"; 
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "登録中にエラーが発生しました。");
            return "auth/register";
        }
    }

    // --- パスワードリセット (SMSフロー) ---
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() { return "auth/forgot-password"; } 

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("phoneNumber") String phoneNumber,
                                        RedirectAttributes redirectAttributes) {
        // SMSでコード送信
        boolean sent = userService.processForgotPasswordBySms(phoneNumber);
        
        if (sent) {
            redirectAttributes.addFlashAttribute("phoneNumber", phoneNumber);
            return "redirect:/verify-code"; // コード入力画面へ
        } else {
            return "redirect:/forgot-password?error"; 
        }
    }

    // --- 認証コード確認画面 ---
    @GetMapping("/verify-code")
    public String verifyCodeForm() { return "auth/verify-code"; }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam("phoneNumber") String phoneNumber,
                             @RequestParam("code") String code,
                             Model model) {
        // コード（トークン）でユーザー検索
        User user = userService.getByResetPasswordToken(code);
        
        // ユーザーが存在し、電話番号も一致すればOK
        if (user != null && user.getPhoneNumber().equals(phoneNumber)) {
            model.addAttribute("token", code);
            return "auth/reset-password"; // パスワード変更画面へ
        } else {
            model.addAttribute("error", "認証コードが無効か、期限切れです。");
            model.addAttribute("phoneNumber", phoneNumber);
            return "auth/verify-code";
        }
    }

    // --- パスワード変更実行 ---
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        User user = userService.getByResetPasswordToken(token);
        if (user == null) {
            model.addAttribute("error", "セッションが無効です。最初からやり直してください。");
            return "auth/forgot-password";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "パスワードが一致しません。");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
        userService.updatePassword(user, password);
        return "redirect:/login?resetSuccess";
    }

    // --- ホーム ---
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
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
}