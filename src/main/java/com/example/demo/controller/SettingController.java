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

	@GetMapping("/edit-username")
    public String editUsername(Model model) {
        // Modelに "form" 属性が含まれていない場合（初回アクセスなど）にのみ、新しいフォームをセット
        // リダイレクトからのフラッシュ属性として "form" が存在する場合は、そのオブジェクトを使用する
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
        
        // 仮のバリデーション・エラー処理（例: ユーザー名が "admin" の場合はエラーとする）
        if ("admin".equalsIgnoreCase(form.getUsername())) {
            // フィールド固有のエラーを追加
            bindingResult.rejectValue("username", "error.username", "そのユーザー名は既に使用されています。");
            
            // フィールドエラーがある場合は、フォーム画面に戻る
            // errorMessageは、フィールドエラーを補足するために使用
            model.addAttribute("errorMessage", "ユーザー名の更新に失敗しました。入力内容を確認してください。");
            
            // formオブジェクトとbindingResultはModelに自動的に含まれるため、returnでフォーム画面に戻ればOK
            return "settings/edit-username"; 
        }

        // --- バリデーションエラーがなかった場合の処理 ---

        // TODO: DB の更新処理（UserService など）
        System.out.println("新しいユーザー名：" + form.getUsername());
        
        // 成功した場合
        redirectAttributes.addFlashAttribute("successMessage", "ユーザー名が正常に更新されました！🎉");
        return "redirect:/settings?updated=username";
    }

    @GetMapping("/edit-email")
    public String editEmail(Model model) {
        model.addAttribute("form", new EditEmailForm());
        return "settings/edit-email";
    }

    @PostMapping("/edit-email")
    public String updateEmail(@ModelAttribute("form") EditEmailForm form, RedirectAttributes redirectAttributes) {
        // TODO: DB 更新処理
        System.out.println("新しいメールアドレス：" + form.getEmail());
        
        // 成功した場合（仮）
        redirectAttributes.addFlashAttribute("successMessage", "メールアドレスが正常に更新されました！📧");
        return "redirect:/settings?updated=email";
    }

    @GetMapping("/change-password")
    public String changePassword(Model model) {
        if (!model.containsAttribute("form")) {
             model.addAttribute("form", new EditPasswordForm());
        }
        return "settings/change-password";
    }

    @PostMapping("/change-password")
    public String updatePassword(@ModelAttribute("form") EditPasswordForm form, 
                                 BindingResult bindingResult, // バリデーション結果を受け取る
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        
        // TODO: パスワード更新処理。現在のパスワードの検証、新しいパスワードの確認、バリデーションなど
        
        // 例: 新しいパスワードと確認用パスワードが一致しない場合（EditPasswordFormにconfirmNewPasswordがあると仮定）
        // if (!form.getNewPassword().equals(form.getConfirmNewPassword())) {
        //    bindingResult.rejectValue("newPassword", "error.newPassword", "新しいパスワードが一致しません。");
        // }
        
        // 例: 現在のパスワードが間違っている場合
        if (!"correct_password".equals(form.getCurrentPassword())) { // 実際はDBと照合する
            model.addAttribute("errorMessage", "現在のパスワードが間違っています。もう一度お試しください。");
            return "settings/change-password"; // エラーがある場合はフォーム画面に戻る
        }
        
        // TODO: パスワード更新処理
        System.out.println("現在:" + form.getCurrentPassword());
        System.out.println("新しい:" + form.getNewPassword());
        
        // 成功した場合
        redirectAttributes.addFlashAttribute("successMessage", "パスワードが正常に変更されました！🔑");
        return "redirect:/settings?updated=password";
    }
}