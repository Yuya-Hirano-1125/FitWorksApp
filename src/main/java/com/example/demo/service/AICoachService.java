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

    /**
     * チャットでの相談に対する回答を生成する
     */
    public String generateCoachingAdvice(User user, List<TrainingRecord> history, String userMessage) {
        String systemPrompt = buildSystemPrompt(user, history);
        String fullPrompt = systemPrompt + "\n\nUser Question: " + userMessage;
        return callGeminiApi(fullPrompt); 
    }

    /**
     * ★追加: トレーニング記録に対するワンポイントアドバイスを生成する
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

    private String buildSystemPrompt(User user, List<TrainingRecord> history) {
        StringBuilder sb = new StringBuilder();
        
        // 基本役割
        sb.append("あなたはフィットネスアプリ『FitWorks』の専属AIトレーナーです。\n");
        sb.append("トレーニングを専門としたチャットをしてください。\n");
        sb.append("ユーザーの要望に合わせて、具体的で効果的なトレーニングメニューを提案してください。\n");
        sb.append("回答はポジティブで親しみやすい口調（日本語）でお願いします。\n\n");
        sb.append("初心者、中級者、上級者別に適したトレーニングを提案してください。】\n");

        // ユーザー情報
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

        sb.append("\n【回答の絶対ルール（厳守）】\n");
        sb.append("1. テキスト内の強調表示（太字）は一切禁止です。アスタリスク記号は絶対に使わないでください。\n");
        sb.append("2. メニュー名や見出しもプレーンテキストで出力してください。\n");
        sb.append("3. 箇条書きには「・」または「- 」のみを使用してください。\n");
        sb.append("4. 構成は「挨拶」→「メニュー（箇条書き）」→「一言」の順で、簡潔にまとめてください。\n");
        sb.append("5. 200文字以内で書いてください。\n");
        sb.append("6. 語尾にムキをつけてください。\n");

        return sb.toString();
    }

    private String callGeminiApi(String prompt) {
        try {
            Client client = Client.builder()
                .apiKey(apiKey)
                .build();
            
            // 応答速度重視のモデルを使用
            GenerateContentResponse response = client.models.generateContent("gemini-2.0-flash", prompt, null);
            return response.text();

        } catch (Exception e) {
            e.printStackTrace();
            return "トレーニングお疲れ様です！その調子で筋肉を育てていきましょう！ムキ！💪";
        }
    }
}