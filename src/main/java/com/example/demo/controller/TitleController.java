package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.AppTitle;
import com.example.demo.entity.User;
import com.example.demo.service.TitleService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/titles")
public class TitleController {

    private final TitleService titleService;
    private final UserService userService;

    public TitleController(TitleService titleService, UserService userService) {
        this.titleService = titleService;
        this.userService = userService;
    }

    @GetMapping
    public String index(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        
        // 戻り値で新規獲得リストを受け取る
        List<AppTitle> newTitles = titleService.checkAndUnlockTitles(user);

        List<AppTitle> unlockedTitles = titleService.getUnlockedTitles(user);
        
        model.addAttribute("allTitles", AppTitle.values());
        model.addAttribute("unlockedTitles", unlockedTitles);
        model.addAttribute("equippedTitle", user.getEquippedTitle());
        
        // 新規獲得がある場合のみモデルに追加
        if (!newTitles.isEmpty()) {
            model.addAttribute("newlyUnlockedTitles", newTitles);
        }
        
        return "titles/title-list";
    }

    @PostMapping("/equip")
    public String equip(@RequestParam(name = "title", required = false) AppTitle title, 
                        Principal principal, 
                        RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        try {
            titleService.equipTitle(user, title);
            redirectAttributes.addFlashAttribute("successMessage", "称号を変更しました！");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/titles";
    }
}