package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> payload, Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        
        // 1. ログインユーザー情報の取得
        User user = null;
        if (authentication != null) {
            user = userService.findByUsername(authentication.getName());
        }

        if (user == null) {
            response.put("reply", "ログインしてください。");
            return response;
        }

        // 2. ユーザーメッセージの取得
        String userMessage = payload.get("message");

        // 3. トレーニング履歴の取得 (最新10件)
        List<TrainingRecord> history = trainingRecordRepository.findTop10ByUser_IdOrderByRecordDateDesc(user.getId());

        // 4. AIサービスへコンテキスト付きで依頼
        // (Serviceメソッドを先ほど修正したものに変更);
        String aiReply = aiCoachService.generateCoachingAdvice(user, history, userMessage);
        
        response.put("reply", aiReply);
        return response;
    }
}






