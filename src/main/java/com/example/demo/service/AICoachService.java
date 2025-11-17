package com.example.demo.service;                                                                                                                            
                                                                                                                                                             
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;                                                                                                               
                                                                                                                                                             
// ★ 修正点: 依存関係エラーを避けるため、インポートを最小化                                                                                                                            
// 以前のcom.google.genaiやcom.google.ai.clientのインポートはすべて削除しました。                                                                                                 
                                                                                                                                                             
@Service                                                                                                                                                     
public class AICoachService {                                                                                                                                
                                                                                                                                                             
    @Value("${gemini.api.key}")                                                                                                                              
    private String geminiApiKey;                                                                                                                             
                                                                                                                                                             
    public String generateResponse(String userMessage) {                                                                                                     
                                                                                                                                                             
        // 依存関係エラー回避のため、ダミーロジックを再配置す。";                                                                                                                      
        try {                                                                                                                                                
            String systemInstructionText = "あなたは専門のAIフィットネスコーチ 'FitBot' です。"                                                                                 
                                         + "ユーザーの目標や体調に基づいて、具体的でモチベーションが上がるトレーニング提案を日本語で、箇条書きを含めて行います。";                                                     
                                                                                                                                                             
            String response = "";                                                                                                                            
                                                                                                                                                             
            if (userMessage == null || userMessage.trim().isEmpty()) {                                                                                       
                response = "メッセージが空です。AIコーチに話しかけてください。";                                                                                                     
            } else if (userMessage.toLowerCase().contains("こんにちは")) {                                                                                        
                 response = "AIコーチのFitBotです。何に関するトレーニングの相談ですか？";                                                                                             
            } else {                                                                                                                                         
                 response = "AIコーチからのダミー応答です: ユーザーの要求 '" + userMessage + "' を処理しました。 "                                                                       
                            + "現在の目標達成のため、次の**トレーニングを開始**することを推奨します。";                                                                                       
            }                                                                                                                                                
                                                                                                                                                             
            return response;                                                                                                                                 
                                                                                                                                                      
        } catch (Exception e) {                                                                                                                              
            System.err.println("Gemini API処理中にエラーが発生しました: " + e.getMessage());                                                                               
            return "AI処理中に致命的なエラーが発生しました。システムログを確認してください。";                                                                                                  
        }                                                                                                                                                    
    }                                                                                                                                                        
}                                                                                                                                                            
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
                                                                                                                                                             
