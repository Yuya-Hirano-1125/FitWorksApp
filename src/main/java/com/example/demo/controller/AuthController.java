package com.example.demo.controller;

// ★ 修正点: java.util.List をインポート
import java.util.List;

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

    // --- ログイン/登録関連 ---
    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm() { return "register"; }

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        // 実際の登録ロジックをここに実装する
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
        
        // 実際のパスワード変更ロジック
        boolean success = true; 
        
        if(success) {
            model.addAttribute("successMessage", "パスワードが正常に変更されました！🎉");
        } else {
            model.addAttribute("errorMessage", "現在のパスワードが正しくありません");
        }
        return "change-password";
    }

    // --- メイン画面への遷移 ---
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

    @GetMapping("/training")
    public String training() { return "training"; }
    
    @GetMapping("/gacha")
    public String gacha() { return "gacha"; } 
    
    // ★ トレーニング記録画面への遷移
    @GetMapping("/training-log")
    public String trainingLog(Model model) { 
        // 仮のデータを作成
        model.addAttribute("records", List.of(
            new Record("2025/11/13", "ベンチプレス", "胸", 85, 5, 3),
            new Record("2025/11/13", "AIおすすめ", "全身", 0, 40, 1),
            new Record("2025/11/12", "デッドリフト", "背中・脚", 100, 3, 3)
        ));
        return "training-log"; 
    }

    @GetMapping("/settings")
    public String settings() { return "settings"; }
}

// データを保持するためのインナークラス (Modelに渡すために使用)
class Record {
    public String date;
    public String name;
    public String part;
    public int weight;
    public int reps;
    public int sets;

    public Record(String date, String name, String part, int weight, int reps, int sets) {
        this.date = date;
        this.name = name;
        this.part = part;
        this.weight = weight;
        this.reps = reps;
        this.sets = sets;
    }

    // Thymeleafがプロパティにアクセスできるよう、getterが必要です
    public String getDate() { return date; }
    public String getName() { return name; }
    public String getPart() { return part; }
    public int getWeight() { return weight; }
    public int getReps() { return reps; }
    public int getSets() { return sets; }
}