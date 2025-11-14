package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// ❗ DTOはご自身のプロジェクトのパスに合わせてください
import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.Message;
import com.example.demo.service.AICoachService;

@RestController
@RequestMapping("/api")
public class AICoachRestController {

    private final AICoachService aiCoachService;
    
    // 初期質問メッセージのベース部分 (Markdown記法)
    private static final String INITIAL_QUESTION_BODY = 
        "💪 最高のトレーニングプランを作成するため、以下の4点をまとめて教えてください！"
        + "\n\n**🎯 トレーニング計画のための質問:**"
        + "\n* **1. 経験レベル**: 初級 / 中級 / 上級"
        + "\n* **2. 可能時間**: 1回あたりのトレーニング時間 (例: 35分)"
        + "\n* **3. 鍛えたい部位**: 胸、背中、脚、全身など"
        + "\n* **4. 場所/器具**: 家（自重・ダンベル）、ジム（全器具）"
        + "\n\n例: **中級、45分、胸と腕、ジム**";

    public AICoachRestController(AICoachService aiCoachService) {
        this.aiCoachService = aiCoachService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Message> getAICoachResponse(@RequestBody ChatRequest chatRequestDto) {
        
        String userMessage = chatRequestDto.getText();
        String userName = chatRequestDto.getUserName();
        String aiResponseText;
        
        boolean hasUserName = userName != null && !userName.trim().isEmpty();
        String greetingName = hasUserName ? userName + "さん、" : "";
        
        try {
            String trimmedMessage = userMessage.trim().toLowerCase();
            
            // 初回挨拶やヘルプ要求の場合
            if (trimmedMessage.isEmpty() || trimmedMessage.contains("こんにちは") || trimmedMessage.contains("ヘルプ") || trimmedMessage.contains("おはよう")) {
                aiResponseText = "**" + greetingName + "AIコーチ FitBot です！**" + INITIAL_QUESTION_BODY;
            } else {
                
                // AIへのプロンプトにユーザー名と200文字制限を組み込む
                String userReference = hasUserName ? "(" + userName + "さん向けに) " : "";
                
                String promptWithInstruction = 
                    userReference + "次の質問に、**回答をMarkdownの箇条書き形式で、200文字以内（簡潔に）**で整理して回答してください。回答の冒頭でユーザー(" + userName + "さん)に話しかけてください。質問: " + userMessage;
                
                aiResponseText = aiCoachService.getGeminiAdvice(promptWithInstruction);
            }
            
        } catch (Exception e) {
            aiResponseText = "❗AI処理中にエラーが発生しました。時間を置いて再度お試しください。";
        }
        
        Message aiMessageDto = new Message("ai", aiResponseText);
        return ResponseEntity.ok(aiMessageDto);
    }
}







