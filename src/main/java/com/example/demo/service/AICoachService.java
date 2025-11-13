package com.example.demo.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class AICoachService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    
    // 全体タイムアウトは60秒を維持
    private static final Duration TIMEOUT = Duration.ofSeconds(60); 

    public AICoachService(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)) 
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * @Cacheable の設定により、同じ userMessage の場合は API をコールせずキャッシュを返します。
     */
    @Cacheable(value = "geminiResponses", key = "#userMessage")
    public String getGeminiAdvice(String userMessage) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "❌ エラー: Gemini APIキーが設定されていません。\napplication.properties に有効なキーを設定してください。";
        }

        try {
            // --- リクエストJSON構築 ---
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // システム指示（AIの性格・目的）
            ObjectNode systemContent = objectMapper.createObjectNode();
            systemContent.put("role", "user");
            ArrayNode systemParts = objectMapper.createArrayNode();
            systemParts.add(objectMapper.createObjectNode().put("text",
                    "あなたはフィットネス専門のAIコーチです。ユーザーの体調・目的に合わせて、" +
                    "日本語で具体的かつ励ましのあるトレーニング提案を行ってください。"));
            systemContent.set("parts", systemParts);
            
            // ユーザー入力
            ObjectNode userContent = objectMapper.createObjectNode();
            userContent.put("role", "user");
            ArrayNode userParts = objectMapper.createArrayNode();
            userParts.add(objectMapper.createObjectNode().put("text", userMessage));
            userContent.set("parts", userParts);
            
            // contents にまとめる
            ArrayNode contents = objectMapper.createArrayNode();
            contents.add(systemContent);
            contents.add(userContent);
            requestBody.set("contents", contents);

            // ★ 修正済み: maxOutputTokensを2048に増やし、応答不完全エラーを回避
            ObjectNode generationConfig = objectMapper.createObjectNode();
            generationConfig.put("maxOutputTokens", 2048); // 👈 2048に増やす (92行目)
            requestBody.set("generationConfig", generationConfig); // 正しいフィールド名を使用

            // --- API呼び出し設定 ---
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + apiKey))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            // --- 503エラー/タイムアウト対応 リトライ処理 (指数関数的バックオフ) ---
            HttpResponse<String> response = null;
            int maxRetries = 3;

            for (int i = 1; i <= maxRetries; i++) {
                try {
                    response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    if (response.statusCode() == 503) {
                        System.out.println("⚠️ Gemini API 過負荷。リトライ中... (" + i + "/" + maxRetries + ")");
                        long sleepTime = 5000L * i; // 5秒, 10秒, 15秒と待機時間を延長
                        Thread.sleep(sleepTime);
                        continue;
                    }
                    break; 

                } catch (HttpTimeoutException e) {
                    if (i < maxRetries) {
                        System.out.println("⚠️ Gemini API リクエストがタイムアウトしました。リトライ中... (" + i + "/" + maxRetries + ")");
                        long sleepTime = 5000L * i; // 5秒, 10秒, 15秒と待機時間を延長
                        Thread.sleep(sleepTime); 
                        continue;
                    }
                    throw e; 
                } catch (IOException e) {
                    if (i < maxRetries) {
                        System.out.println("⚠️ API通信エラーが発生しました。リトライ中... (" + i + "/" + maxRetries + ") 詳細: " + e.getMessage());
                        Thread.sleep(1000L * i);
                        continue;
                    }
                    throw e;
                }
            }
            
            // --- ステータスコード確認、レスポンス解析処理 ---
            String responseJson = response.body();
            if (response.statusCode() >= 400) {
                if (response.statusCode() == 503) {
                    return "⚠️ 現在AIサーバーが混み合っています。数秒後にもう一度お試しください。";
                }
                // 400エラーなど、他のエラーは詳細を表示
                return "API通信エラー (HTTP Status: " + response.statusCode() + ")\n詳細: " + responseJson;
            }
            
            ObjectNode responseNode = (ObjectNode) objectMapper.readTree(responseJson);
            
            // レスポンスのJSON構造をチェック
            if (responseNode.has("candidates")
                    && responseNode.get("candidates").get(0).has("content")
                    && responseNode.get("candidates").get(0).get("content").has("parts")) {
                        
                return responseNode.get("candidates").get(0)
                        .get("content").get("parts").get(0)
                        .get("text").asText();
                        
            } else if (responseNode.has("error")) {
                return "Gemini APIエラー: " + responseNode.get("error").get("message").asText();
                
            } else {
                // 応答が不完全な場合（SAFETYブロックなど）
                String finishReason = "不明";
                if (responseNode.has("candidates") && responseNode.get("candidates").get(0).has("finishReason")) {
                     finishReason = responseNode.get("candidates").get(0).get("finishReason").asText();
                }
                return "⚠️ AIコーチからの応答が不完全です。 (終了理由: " + finishReason + " )";
            }
            
        } catch (HttpTimeoutException e) {
            e.printStackTrace();
            return "❌ 接続タイムアウト: AIコーチからの応答が指定時間内に得られませんでした。しばらく時間をおいてお試しください。";
        } catch (Exception e) {
            e.printStackTrace();
            return "❗予期せぬエラーが発生しました。\n詳細: " + e.getMessage();
        }
    }
}