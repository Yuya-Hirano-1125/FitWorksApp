package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.Message;
import com.example.demo.service.AICoachService; 

@RestController
@RequestMapping("/api")
public class AICoachRestController {

    private final AICoachService aiCoachService;

    // 初期質問メッセージ (HTMLタグとMarkdown風記法を含む)
    private static final String INITIAL_QUESTION = 
        "**AIコーチ FitBot です！**💪 最高のトレーニングプランを作成するため、以下の4点をまとめて教えてください！"
        + "<br><br>1. **経験レベル** (初級 / 中級 / 上級)"
        + "<br>2. **可能時間** (1回あたりのトレーニング時間)"
        + "<br>3. **鍛えたい部位** (胸、背中、脚、全身など)"
        + "<br>4. **場所/器具** (家、ジム、ダンベル利用など)"
        + "<br><br>例: **中級、45分、胸と腕、ジム**";

    public AICoachRestController(AICoachService aiCoachService) {
        this.aiCoachService = aiCoachService;
    }

    // AJAX POSTリクエストを受け付け、JSONで回答を返す
    @PostMapping("/chat")
    public ResponseEntity<Message> getAICoachResponse(@RequestBody Message userMessageDto) {
        
        String userMessage = userMessageDto.getText();
        String aiResponseText;
        
        try {
            String trimmedMessage = userMessage.trim().toLowerCase();

            if (trimmedMessage.isEmpty() || trimmedMessage.contains("こんにちは") || trimmedMessage.contains("ヘルプ")) {
                 aiResponseText = INITIAL_QUESTION;
            } else {
                // 実際の AI サービス呼び出し
                aiResponseText = aiCoachService.getGeminiAdvice(userMessage); 
            }
        } catch (Exception e) {
            aiResponseText = "❗AI処理中にエラーが発生しました: " + e.getMessage();
        }

        // Message DTOをJSONとして返す
        Message aiMessageDto = new Message("ai", aiResponseText);
        return ResponseEntity.ok(aiMessageDto); 
    }
}