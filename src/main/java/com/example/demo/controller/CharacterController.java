package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;

@Controller
public class CharacterController {

    private final UserService userService;

    public CharacterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/character")
    public String character(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                model.addAttribute("username", user.getUsername());
                model.addAttribute("level", user.getLevel());
                model.addAttribute("experiencePoints", user.getExperiencePoints());
                model.addAttribute("requiredXp", user.calculateRequiredXp());
                model.addAttribute("progressPercent", user.getProgressPercent());
                model.addAttribute("equippedBackgroundItem", user.getEquippedBackgroundItem());
                model.addAttribute("equippedCostumeItem", user.getEquippedCostumeItem());
                // 必要なら inventory も追加
                // model.addAttribute("inventory", itemService.findByUser(user));
            }
        }
        return "character"; // → templates/character.html を返す
    }
}
