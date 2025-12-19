package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
//★追加: AuthenticationPrincipal を解決するためのインポート
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.User;
import com.example.demo.form.EditEmailForm;
import com.example.demo.form.EditPasswordForm;
import com.example.demo.form.EditUsernameForm;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.UserService;

@Controller
public class SettingController {

    @Autowired
    private UserService userService;
    
 // ★追加: userDetailsService フィールドを追加
    private final CustomUserDetailsService userDetailsService;
    
 // ★修正: 引数に CustomUserDetailsService を追加して保存する
    public SettingController(UserService userService, CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService; // ★追加
    }
    
    
    @Autowired
    private UserRepository userRepository;

    // ヘルパーメソッド: ログインユーザー取得
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userService.findByUsername(auth.getName());
    }

    // -------------------------
    // 設定画面 (表示)
    // -------------------------
    @GetMapping("/settings")
    public String showSettings(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        // ★★★ 修正: ユーザーの現在の設定値をモデルに渡す
        model.addAttribute("user", user);
        
        return "settings/settings";
    }

    // -------------------------
    // ★★★ API: 設定の更新 (Ajax用) ★★★
    // -------------------------
    @PostMapping("/api/settings/update")
    @ResponseBody
    public Map<String, Object> updateSettings(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        User user = getCurrentUser();
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "ログインしてください");
            return response;
        }

        try {
            // 送られてきたキーに応じて値を更新
            if (payload.containsKey("notificationTrainingReminder")) {
                user.setNotificationTrainingReminder((Boolean) payload.get("notificationTrainingReminder"));
            }
            if (payload.containsKey("notificationAiSuggestion")) {
                user.setNotificationAiSuggestion((Boolean) payload.get("notificationAiSuggestion"));
            }
            if (payload.containsKey("notificationProgressReport")) {
                user.setNotificationProgressReport((Boolean) payload.get("notificationProgressReport"));
            }
            if (payload.containsKey("theme")) {
                user.setTheme((String) payload.get("theme"));
            }

            userRepository.save(user);
            response.put("success", true);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
        }
        
        return response;
    }
 // 1. ユーザー名変更画面の表示
    @GetMapping("/edit-username")
    public String editUsername(Model model) {
        model.addAttribute("editUsernameForm", new EditUsernameForm());
        return "settings/edit-username";
    }

    // 2. ユーザー名変更の処理実行
 // 引数に RedirectAttributes を追加
    @PostMapping("/edit-username")
    public String updateUsername(@Validated @ModelAttribute EditUsernameForm form,
                                 BindingResult result,
                                 @AuthenticationPrincipal UserDetails currentUser,
                                 RedirectAttributes redirectAttributes, // ★追加: これでメッセージを持ち運べます
                                 Model model) {
        
        // バリデーションエラー時は元の画面に戻す
        if (result.hasErrors()) {
            return "settings/edit-username";
        }

        try {
            // DB更新
            userService.updateUsername(currentUser.getUsername(), form.getNewUsername());

            // セキュリティコンテキスト（ログイン情報）の更新
            updateSecurityContext(form.getNewUsername());

            // ★追加: 完了メッセージをセット（リダイレクト先に一度だけ表示される）
            redirectAttributes.addFlashAttribute("successMessage", "ユーザー名が正常に変更されました！✨");

            // ★変更: 設定一覧画面へリダイレクト
            return "redirect:/settings"; 

        } catch (IllegalArgumentException e) {
            // 重複エラーなどは元の画面で表示
            model.addAttribute("errorMessage", e.getMessage());
            return "settings/edit-username";
        }
    }

    /**
     * Spring Securityのコンテキスト内のユーザー情報を更新するヘルパーメソッド
     */
    private void updateSecurityContext(String newUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // 現在のPrincipalから新しいUserDetailsを作成（または既存のものをラップしなおす）
        // ここでは簡易的に、現在のAuthenticationを維持しつつPrincipalの名前が変わった前提で再設定します
        // ※ 本格的には UserDetailsService からロードし直すのが最も安全です
        
        User newUser = new User(); // ここはEntityのUserではなくUserDetailsの実装に合わせて調整
        // CustomUserDetailsを使用している場合は、以下のように再ロードが推奨されます:
        UserDetails newUserDetails = userDetailsService.loadUserByUsername(newUsername);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                newUserDetails, auth.getCredentials(), auth.getAuthorities());
        
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    // -------------------------
    // メールアドレス編集
    // -------------------------
    @GetMapping("/edit-email")
    public String editEmail(Model model) {
        model.addAttribute("form", new EditEmailForm());
        return "settings/edit-email";
    }

    @PostMapping("/edit-email")
    public String updateEmail(@ModelAttribute("form") EditEmailForm form, RedirectAttributes redirectAttributes) {
        // TODO: DB 更新処理
        System.out.println("新しいメールアドレス：" + form.getEmail());
        
        redirectAttributes.addFlashAttribute("successMessage", "メールアドレスが正常に更新されました！📧");
        return "redirect:/settings?updated=email";
    }

    // -------------------------
    // パスワード変更
    // -------------------------
    @GetMapping("/change-password")
    public String changePassword(Model model) {
        if (!model.containsAttribute("form")) {
             model.addAttribute("form", new EditPasswordForm());
        }
        return "settings/change-password";
    }

    @PostMapping("/change-password")
    public String updatePassword(@ModelAttribute("form") EditPasswordForm form, 
                                 BindingResult bindingResult, 
                                 RedirectAttributes redirectAttributes, 
                                 Model model) {
        
        if (!"correct_password".equals(form.getCurrentPassword())) { // 実際はDBと照合する
            model.addAttribute("errorMessage", "現在のパスワードが間違っています。もう一度お試しください。");
            return "settings/change-password";
        }
        
        // TODO: パスワード更新処理
        System.out.println("現在:" + form.getCurrentPassword());
        System.out.println("新しい:" + form.getNewPassword());
        
        redirectAttributes.addFlashAttribute("successMessage", "パスワードが正常に変更されました！🔑");
        return "redirect:/settings?updated=password";
    }

    // -------------------------
    // アカウント削除関連
    // -------------------------
    @GetMapping("/delete-account")
    public String deleteAccountPage() {
        return "settings/delete-account";
    }

    @PostMapping("/delete-account")
    public String deleteAccountConfirm() {
        // TODO: 実際の削除処理（UserServiceなどでDBからユーザーを削除）
        System.out.println("アカウント削除処理を実行しました。");
        return "settings/goodbye";
    }
    
    // 利用規約画面
    @GetMapping("/terms")
    public String terms() {
        return "settings/terms"; 
    }
    
    // プライバシーポリシー画面
    @GetMapping("/privacy")
    public String privacy() {
        return "settings/privacy"; 
    }
    
    // FaQ画面
    @GetMapping("/faq")
    public String faq() {
        return "settings/faq"; 
    }
}