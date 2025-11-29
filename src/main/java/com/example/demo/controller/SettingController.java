package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import com.example.demo.service.UserService;

@Controller
public class SettingController {

    @Autowired
    private UserService userService;
    
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

    // -------------------------
    // ユーザー名編集
    // -------------------------
    @GetMapping("/edit-username")
    public String editUsername(Model model) {
        if (!model.containsAttribute("form")) {
             model.addAttribute("form", new EditUsernameForm());
        }
        return "settings/edit-username";
    }

    @PostMapping("/edit-username")
    public String updateUsername(@ModelAttribute("form") EditUsernameForm form, 
                                 BindingResult bindingResult, 
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        
        if ("admin".equalsIgnoreCase(form.getUsername())) {
            bindingResult.rejectValue("username", "error.username", "そのユーザー名は既に使用されています。");
            model.addAttribute("errorMessage", "ユーザー名の更新に失敗しました。入力内容を確認してください。");
            return "settings/edit-username"; 
        }

        // TODO: DB の更新処理
        System.out.println("新しいユーザー名：" + form.getUsername());
        
        redirectAttributes.addFlashAttribute("successMessage", "ユーザー名が正常に更新されました！🎉");
        return "redirect:/settings?updated=username";
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