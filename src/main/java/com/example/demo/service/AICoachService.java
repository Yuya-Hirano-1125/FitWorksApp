package com.example.demo.service;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Service
public class AICoachService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private Client client;

    // アプリケーション起動時にクライアントを初期化
    @PostConstruct
    public void init() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {            
            // APIキーを使用してGeminiクライアントを作成
            this.client = Client.builder()
                .apiKey(geminiApiKey)
                .build();
        } else {
            System.err.println("Warning: gemini.api.key is not set in application.properties");
        }
    }

    public String generateResponse(String prompt) {
        // クライアントが初期化されていない場合のエラーハンドリング
        if (client == null) {
            return "エラー: APIキーが設定されていないため、AI機能を利用できません。管理者にお問い合わせください。";
        }

        try {
            // Geminiモデルを指定 (高速でチャット向きな gemini-1.5-flash を推奨)
            String modelId = "gemini-2.5-flash";

            // APIを呼び出してコンテンツを生成
            // 第3引数のconfigはnullの場合、デフォルト設定が使用されます
            GenerateContentResponse response = client.models.generateContent(modelId, prompt, null);

            // 生成されたテキストを返す
            return response.text();

        } catch (Exception e) {
            // エラー発生時のログ出力とユーザーへの通知
            e.printStackTrace();
            return "申し訳ありません。AIの処理中にエラーが発生しました (" + e.getMessage() + ")。しばらく待ってから再度お試しください。";
        }
    }
}










                                                                            

