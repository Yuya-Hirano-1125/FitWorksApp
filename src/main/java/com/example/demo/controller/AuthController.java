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

    // --- ログイン/登録関連 ---
    @GetMapping("/login")
    public String login() { return "auth/login"; } 

    @GetMapping("/register")
    public String registerForm() { return "auth/register"; } 

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        // 実際の登録ロジックをここに実装する
        model.addAttribute("message", "登録が完了しました。ログインしてください。");
        return "auth/login"; 
    }

    // --- パスワードリセット ---
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() { return "auth/forgot-password"; } 

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes) {
        boolean emailFoundAndSent = true; 
        if (emailFoundAndSent) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "パスワードリセット用のリンクをメールアドレス " + email + " 宛に送信しました。"); // ★ セミコロンを修正
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "そのメールアドレスは登録されていません。");
        }
        return "redirect:/forgot-password";
    }

    // --- パスワード変更 ---
    /*@PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "新しいパスワードが一致しません");
            return "change-password";
        }
        
        // 実際のパスワード変更ロジック
        boolean success = true; 
        
        if(success) {
            model.addAttribute("successMessage", "パスワードが正常に変更されました！🎉");
        } else {
            model.addAttribute("errorMessage", "現在のパスワードが正しくありません");
        }
        return "change-password";
    }*/

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
        return "misc/home"; // 修正済み
    }

    // @GetMapping("/training") // <--- 削除しました。TrainingControllerに一任されます。
    // public String training() { return "training"; } 
    
    // NOTE: /gacha のマッピングは GachaController に移管されたため、削除。
    
    
    // NOTE: /training-log のマッピングは TrainingController に移管されたため、削除。

    @GetMapping("/settings")
    public String settings() { return "settings/settings"; } // 修正済み
}

























































