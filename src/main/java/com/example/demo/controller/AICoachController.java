// AICoachController.java
package com.example.demo.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
// ... (他のインポート) ...

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.AICoachService;

@RestController
@RequestMapping("/api")
public class AICoachController {

    private final AICoachService aiCoachService;
    private final Executor taskExecutor; 

    public AICoachController(AICoachService aiCoachService, Executor taskExecutor) {
        this.aiCoachService = aiCoachService;
        this.taskExecutor = taskExecutor;
    }

    @PostMapping("/chat")
    public CompletableFuture<String> getAICoachResponse(@RequestParam("message") String message) {

        String lowerMessage = message.trim().toLowerCase();

        // ★ 質問ロジック (対話型)
        if (lowerMessage.isEmpty() || lowerMessage.contains("こんにちは") || lowerMessage.contains("目標") || lowerMessage.contains("体調") || lowerMessage.contains("トレーニング") || lowerMessage.contains("太くしたい") || lowerMessage.contains("鍛えたい")) {
            
            String initialResponse = """
                **こんにちは！AIコーチのFitBotです。** 💪
                
                あなたの今日の**トレーニングの目標や体調**について教えていただけますか？最適なメニューを提案します！
                
                ---
                
                なお、FitBotにメニューを組ませるには、以下の4点をまとめてお伝えください。
                
                ## 🎯 トレーニング計画のための質問
                
                | 質問 | 回答オプション |
                | :---: | :---: |
                | **1. 鍛えたい部位** | 腕、胸、脚、背中、腹筋、全身から選択 |
                | **2. トレーニング経験** | 初級、中級、上級から選択 |
                | **3. 一日のトレーニング時間** | **5分刻みで5分〜60分まで指定** (例: 35分) |
                | **4. トレーニング場所** | 家（自重・ダンベル）、ジム（全器具）から選択 |
                
                例: 「**腕、中級、30分、ジム**」""";
            
            // APIをコールせず、即座に質問を非同期で返す
            return CompletableFuture.completedFuture(initialResponse);
        }

        // ★ 修正箇所: Executorを明示的に指定
        // ユーザーが質問に回答した場合、カスタムExecutor (taskExecutor) でAI処理を実行する
        return CompletableFuture.supplyAsync(() -> {
            return aiCoachService.getGeminiAdvice(message);
        }, taskExecutor); // <-- ここでtaskExecutorを使用
    }
}



