package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.form.EditEmailForm;
import com.example.demo.form.EditPasswordForm;
import com.example.demo.form.EditUsernameForm;

@Controller
public class SettingController {

    @GetMapping("/edit-username")
    public String editUsername(Model model) {
        model.addAttribute("form", new EditUsernameForm());
        return "settings/edit-username";
    }

    @PostMapping("/edit-username")
    public String updateUsername(@ModelAttribute("form") EditUsernameForm form) {
        // TODO: DB の更新処理（UserService など）
        System.out.println("新しいユーザー名：" + form.getUsername());
        return "redirect:/settings?updated=username";
    }

    @GetMapping("/edit-email")
    public String editEmail(Model model) {
        model.addAttribute("form", new EditEmailForm());
        return "settings/edit-email";
    }

    @PostMapping("/edit-email")
    public String updateEmail(@ModelAttribute("form") EditEmailForm form) {
        // TODO: DB 更新処理
        System.out.println("新しいメールアドレス：" + form.getEmail());
        return "redirect:/settings?updated=email";
    }

    @GetMapping("/change-password")
    public String changePassword(Model model) {
        model.addAttribute("form", new EditPasswordForm());
        return "settings/change-password";
    }

    @PostMapping("/change-password")
    public String updatePassword(@ModelAttribute("form") EditPasswordForm form) {
        // TODO: パスワード更新処理
        System.out.println("現在:" + form.getCurrentPassword());
        System.out.println("新しい:" + form.getNewPassword());
        return "redirect:/settings?updated=password";
    }
}

