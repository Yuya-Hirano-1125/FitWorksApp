package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Service
public class AICoachService {

    @Value("${gemini.api.key}")
    private String apiKey;

    public String generateCoachingAdvice(User user, List<TrainingRecord> history, String userMessage) {
        String systemPrompt = buildSystemPrompt(user, history);
        String fullPrompt = systemPrompt + "\n\nUser Question: " + userMessage;
        return callGeminiApi(fullPrompt); 
    }

    private String buildSystemPrompt(User user, List<TrainingRecord> history) {
        StringBuilder sb = new StringBuilder();
        
        // 基本役割
        sb.append("あなたはフィットネスアプリ『FitWorks』の専属AIトレーナーです。\n");
        sb.append("ユーザーの要望に合わせて、具体的で効果的なトレーニングメニューを提案してください。\n");
        sb.append("回答はポジティブで親しみやすい口調（日本語）でお願いします。\n\n");

        // ★ 修正点1: レベルによる勝手な決めつけを廃止
        // アプリのLvは単なる継続度として伝え、フィットネスレベルはユーザーの申告を優先するよう指示
        sb.append("【ユーザー情報】\n");
        sb.append("- 名前: ").append(user.getUsername()).append("\n");
        sb.append("- アプリ利用レベル: Lv.").append(user.getLevel()).append("\n");
        sb.append("※重要: 「アプリ利用レベル」はフィットネス経験値とは異なります。ユーザーがチャットで「初心者」「上級者」などを申告した場合は、その情報を最優先してください。\n\n");
        
        // トレーニング履歴
        sb.append("【直近の履歴】\n");
        if (history != null && !history.isEmpty()) {
            for (TrainingRecord record : history) {
                String menu = "WEIGHT".equals(record.getType()) ? record.getExerciseName() : record.getCardioType();
                sb.append("- ").append(record.getRecordDate()).append(": ").append(menu).append("\n");
            }
        } else {
            sb.append("- 記録なし\n");
        }

        // ★ 修正点2: 出力フォーマットの改善（アスタリスク禁止）
        sb.append("\n【回答のルール】\n");
        sb.append("1. **回答には「*」（アスタリスク）を使用しないでください。** マークダウンの太字強調などは不要です。\n");
        sb.append("2. 箇条書きには「・」や「- 」を使用し、見やすく整形してください。\n");
        sb.append("3. メニュー提案は、種目名と回数/セット数を簡潔に提示してください。\n");
        sb.append("4. 長い前置きは省略し、スマホで読みやすい長さ（短め）にまとめてください。\n");

        return sb.toString();
    }

    // determineLevelLabel メソッドは不要になったため削除しました

    private String callGeminiApi(String prompt) {
        try {
            Client client = Client.builder()
                .apiKey(apiKey)
                .build();
            
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            return response.text();

        } catch (Exception e) {
            e.printStackTrace();
            return "通信エラーが発生しました。もう一度試してください。";
        }
    }
}

















