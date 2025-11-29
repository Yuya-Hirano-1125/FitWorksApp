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
        return "start"; // templates/start.html を表示
    }

    // --- ログイン/登録関連 ---
    @GetMapping("/login")
    public String login() { return "auth/login"; } 

    @GetMapping("/register")
    public String registerForm() { return "auth/register"; } 

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        // 実際の登録ロジックはUserService等で行う想定
        model.addAttribute("message", "登録が完了しました。ログインしてください。");
        return "auth/login"; 
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
            // ユーザー情報を取得
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

    // 重複していた settings メソッドを削除しました
}