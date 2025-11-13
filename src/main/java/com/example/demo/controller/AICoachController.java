package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.AICoachService;

@RestController 
public class AICoachController {

    private final AICoachService aiCoachService;

    public AICoachController(AICoachService aiCoachService) {
        this.aiCoachService = aiCoachService;
    }

    /**
     * フロントエンドからのメッセージを受け取り、AIコーチサービスに渡す
     * URL: /api/chat (POST)
     */
    @PostMapping("/api/chat")
    public String getAICoachResponse(@RequestParam("message") String message) {
        String aiResponse = aiCoachService.getGeminiAdvice(message);
        return aiResponse;
    }
}







