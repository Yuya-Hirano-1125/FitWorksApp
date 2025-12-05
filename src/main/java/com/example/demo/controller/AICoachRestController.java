package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // 追加

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.MealRecord; // 追加
import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.service.AICoachService;
import com.example.demo.service.MealService; // 追加
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
    
    @Autowired
    private MealService mealService; // 追加

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
        List<TrainingRecord> trainingHistory = trainingRecordRepository.findTop10ByUser_IdOrderByRecordDateDesc(user.getId());

        // 4. 食事履歴の取得 (追加: 最新10件に絞る)
        List<MealRecord> allMeals = mealService.getMealRecordsByUser(user);
        List<MealRecord> mealHistory = allMeals.stream()
                                               .limit(10)
                                               .collect(Collectors.toList());

        // 5. AIサービスへコンテキスト付きで依頼 (引数を追加)
        String aiReply = aiCoachService.generateCoachingAdvice(user, trainingHistory, mealHistory, userMessage);
        
        response.put("reply", aiReply);
        return response;
    }
}