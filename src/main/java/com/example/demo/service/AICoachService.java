package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

@Service
public class AICoachService {

    @Value("${gemini.api.key}")
    private String apiKey;

    /**
     * チャットでの相談に対する回答を生成する
     */
    public String generateCoachingAdvice(User user, List<TrainingRecord> history, String userMessage) {
        String systemPrompt = buildSystemPrompt(user, history);
        String fullPrompt = systemPrompt + "\n\nUser Question: " + userMessage;
        return callGeminiApi(fullPrompt); 
    }

    /**
     * トレーニング記録に対するワンポイントアドバイスを生成する
     */
    public String generateTrainingAdvice(User user, String trainingSummary) {
        StringBuilder sb = new StringBuilder();
        sb.append("あなたはフィットネスアプリの専属AIトレーナーです。\n");
        sb.append("ユーザーがトレーニングを記録しました。この努力を盛大に褒めて、モチベーションを上げてください。\n");
        sb.append("【ユーザー】").append(user.getUsername()).append("さん\n");
        sb.append("【行ったトレーニング】").append(trainingSummary).append("\n");
        
        sb.append("\nルール: 100文字以内で簡潔に。熱血かつポジティブに。絵文字(💪🔥など)を多用して。語尾にムキをつけてください。");

        return callGeminiApi(sb.toString());
    }

    /**
     * ★追加: 食事画像を解析して栄養素を推定する
     */
    public String analyzeMealImage(MultipartFile imageFile) {
        try {
            Client client = Client.builder()
                .apiKey(apiKey)
                .build();

            // 1. 画像データを準備
            String mimeType = imageFile.getContentType();
            if (mimeType == null) mimeType = "image/jpeg";
            byte[] imageBytes = imageFile.getBytes();
            Part imagePart = Part.fromBytes(imageBytes, mimeType);

            // 2. プロンプト（JSON形式での出力を強制）
            String promptText = """
                この食事の画像を分析してください。
                以下の情報をJSON形式で出力してください。Markdownのコードブロックは不要です。純粋なJSON文字列のみを返してください。
                推測で構わないので、必ず数値を埋めてください。

                {
                    "content": "料理名（日本語）",
                    "calories": カロリー(整数),
                    "protein": タンパク質g(数値),
                    "fat": 脂質g(数値),
                    "carbohydrate": 炭水化物g(数値),
                    "comment": "AIからの短いコメント（50文字以内）"
                }
                """;
            Part textPart = Part.fromText(promptText);

            Content content = Content.fromParts(textPart, imagePart);

            // 3. API呼び出し (Gemini 2.0 Flash)
            GenerateContentResponse response = client.models.generateContent("gemini-2.0-flash", content, null);
            
            String responseText = response.text();
            
            // Markdownの除去 (```json ... ```)
            return responseText.replaceAll("```json", "").replaceAll("```", "").trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"AI解析に失敗しました\"}";
        }
    }

    private String buildSystemPrompt(User user, List<TrainingRecord> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("あなたはフィットネスアプリ『FitWorks』の専属AIトレーナーです。\n");
        sb.append("ユーザーの要望に合わせて、具体的で効果的なトレーニングメニューを提案してください。\n");
        sb.append("回答はポジティブで親しみやすい口調（日本語）でお願いします。\n\n");

        sb.append("【ユーザー情報】\n");
        sb.append("- 名前: ").append(user.getUsername()).append("\n");
        sb.append("- アプリ利用レベル: Lv.").append(user.getLevel()).append("\n");
        
        sb.append("【直近の履歴】\n");
        if (history != null && !history.isEmpty()) {
            for (TrainingRecord record : history) {
                String menu = "WEIGHT".equals(record.getType()) ? record.getExerciseName() : record.getCardioType();
                sb.append("- ").append(record.getRecordDate()).append(": ").append(menu).append("\n");
            }
        } else {
            sb.append("- 記録なし\n");
        }

        sb.append("\n【回答の絶対ルール】\n");
        sb.append("1. 強調表示（太字）禁止。\n");
        sb.append("2. 200文字以内。\n");
        sb.append("3. 語尾にムキをつけてください。\n");

        return sb.toString();
    }

    private String callGeminiApi(String prompt) {
        try {
            Client client = Client.builder().apiKey(apiKey).build();
            GenerateContentResponse response = client.models.generateContent("gemini-2.0-flash", prompt, null);
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "エラーが発生しましたムキ！";
        }
    }
}