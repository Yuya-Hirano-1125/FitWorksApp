package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

/**
 * Gemini APIをWebClientを使用して直接呼び出すサービス。
 * SDKのパッケージ変更に依存しない、最も安定した実装です。
 */
@Service
public class AICoachService {

    // application.properties から API キーを読み込む
    @Value("${gemini.api.key}")
    private String apiKey;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // WebClientのベースURLをコンストラクタで設定
    public AICoachService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
            .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/")
            .build();
        this.objectMapper = objectMapper;
    }
    
    public String getGeminiAdvice(String userMessage) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_ACTUAL_GEMINI_API_KEY")) {
            return "エラー: Gemini APIキーが設定されていません。";
        }

        try {
            // 1. リクエストボディ (JSON) の構築
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // システムプロンプトの設定 (AIの役割定義)
            ObjectNode systemInstruction = objectMapper.createObjectNode()
                .put("role", "system")
                .put("content", "あなたはフィットネス専門のAIコーチです。ユーザーの目標や体調に基づいて、具体的でモチベーションが上がるトレーニング提案を日本語で、箇条書きを含めて行います。");
            
            // ユーザーメッセージの構築
            ObjectNode userMessageNode = objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", userMessage);

            ArrayNode contents = objectMapper.createArrayNode()
                .add(systemInstruction)
                .add(userMessageNode);

            requestBody.set("contents", contents);
            
            // 2. WebClientでAPIを呼び出し
            // uri() にはベースURL後のパスのみを指定 (例: /v1beta/models/ から続く部分)
            Mono<String> responseMono = webClient.post()
                .uri("gemini-2.5-flash:generateContent?key=" + apiKey) 
                .header("Content-Type", "application/json")
                .bodyValue(requestBody.toString())
                .retrieve()
                // 4xx, 5xx エラーをチェックし、RuntimeExceptionを投げる
                .onStatus(status -> status.isError(), clientResponse ->
                    Mono.error(new RuntimeException("API Error: Status " + clientResponse.statusCode() + " - " + clientResponse.bodyToMono(String.class).block()))
                )
                .bodyToMono(String.class);
            
            String responseJson = responseMono.block(); // 同期的に結果を待機
            
            // 3. 応答JSONからメッセージテキストを抽出
            ObjectNode responseNode = (ObjectNode) objectMapper.readTree(responseJson);
            
            if (responseNode.has("candidates") && responseNode.get("candidates").get(0).has("content")) {
                return responseNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
            } else if (responseNode.has("error")) {
                return "Gemini APIエラー: " + responseNode.get("error").get("message").asText();
            } else {
                return "AIコーチからの応答が不完全です。";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "予期せぬエラーが発生しました。詳細: " + e.getMessage();
        }
    }
}