package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.service.AICoachService;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/ai-coach")
public class AICoachRestController {

    @Autowired
    private AICoachService aiCoachService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TrainingRecordRepository trainingRecordRepository;

    // ★追加: ホーム画面用のアドバイスを取得するAPI
    @GetMapping("/home-advice")
    public String getHomeAdvice(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "ログインしてトレーニングを始めるムキ！";
        }
        
        // ユーザー情報を取得してAIサービスへ
        User user = userService.findByUsername(userDetails.getUsername());
        return aiCoachService.generateHomeAdvice(user);
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> payload, Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        
        User user = null;
        if (authentication != null) {
            user = userService.findByUsername(authentication.getName());
        }

        if (user == null) {
            response.put("reply", "ログインしてください。");
            return response;
        }

        String userMessage = payload.get("message");
        List<TrainingRecord> history = trainingRecordRepository.findTop10ByUser_IdOrderByRecordDateDesc(user.getId());

        String aiReply = aiCoachService.generateCoachingAdvice(user, history, userMessage);
        
        response.put("reply", aiReply);
        return response;
    }
}