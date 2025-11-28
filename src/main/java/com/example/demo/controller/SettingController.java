package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // バリデーション結果のインポート
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // リダイレクト時に属性を渡すためのインポート

import com.example.demo.form.EditEmailForm;
import com.example.demo.form.EditPasswordForm;
import com.example.demo.form.EditUsernameForm;

@Controller
public class SettingController {

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

    /**
     * アカウント削除確認画面
     */
    @GetMapping("/delete-account")
    public String deleteAccountPage() {
        // templates/settings/delete-account.html を返す
        return "settings/delete-account";
    }

    /**
     * アカウント削除処理 → 完了画面へ遷移
     */
    @PostMapping("/delete-account")
    public String deleteAccountConfirm() {
        // TODO: 実際の削除処理（UserServiceなどでDBからユーザーを削除）
        System.out.println("アカウント削除処理を実行しました。");

        // 削除完了画面を返す
        return "settings/goodbye";
    }
    
    
 // 利用規約画面
    @GetMapping("/terms")
    public String terms() {
        // templates/misc/terms.html を表示するよう指定
        return "settings/terms"; 
    }
    
 // プライバシーポリシー画面
    @GetMapping("/privacy")
    public String privacy() {
        return "settings/privacy"; // templates/settings/privacy.html を表示
    }
}
