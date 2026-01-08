package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class AICoachController { 
    
    // GET /ai-coach リクエストを担当
    @GetMapping("/ai-coach")
    public String aiCoachPage(
        @AuthenticationPrincipal UserDetails userDetails, 
        Model model
    ) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        } else {
            model.addAttribute("username", "ゲスト");
        }
        return "aicoach/ai-coach-chat"; // 修正
    }
}



