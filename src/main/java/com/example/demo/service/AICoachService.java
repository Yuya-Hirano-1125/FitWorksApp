package com.example.demo.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.entity.MealRecord;
import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Service
public class AICoachService {

    @Value("${gemini.api.key}")
    private String apiKey;

    /**
     * チャット画面でのトレーニング・食事相談への回答を生成する
     * (前回の修正を含み、食事履歴も考慮できるようにしています)
     */
    public String generateCoachingAdvice(User user, List<TrainingRecord> trainingHistory, List<MealRecord> mealHistory, String userMessage) {
        String systemPrompt = buildSystemPrompt(user, trainingHistory, mealHistory);
        String fullPrompt = systemPrompt + "\n\nUser Question: " + userMessage;
        return callGeminiApi(fullPrompt, "gemini-2.0-flash"); 
    }
    
    /**
     * 後方互換性用（食事履歴を渡さない呼び出し用）
     */
    public String generateCoachingAdvice(User user, List<TrainingRecord> history, String userMessage) {
        return generateCoachingAdvice(user, history, null, userMessage);
    }

    /**
     * ★新規追加: 食事記録に対するワンポイントアドバイスを生成する
     */
    public String generateMealAdvice(User user, MealRecord mealRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("あなたはフィットネスアプリの専属AIトレーナーです。\n");
        sb.append("ユーザーが食事を記録しました。内容に対して、ポジティブで親しみやすいフィードバックをしてください。\n");
        sb.append("栄養バランスやカロリー、食事のタイミング（").append(mealRecord.getMealType()).append("）などを考慮して褒める、または簡単なアドバイスをしてください。\n");
        sb.append("【ユーザー】").append(user.getUsername()).append("さん\n");
        sb.append("【食事内容】").append(mealRecord.getContent()).append("\n");
        if (mealRecord.getCalories() != null) {
            sb.append("【カロリー】").append(mealRecord.getCalories()).append("kcal\n");
        }
        sb.append("【PFC】P:").append(mealRecord.getProtein()).append("g, F:")
          .append(mealRecord.getFat()).append("g, C:").append(mealRecord.getCarbohydrate()).append("g\n");
        
        sb.append("\nルール: 100文字以内で簡潔に。絵文字を使って元気づけてください。語尾にムキをつけてください。");

        return callGeminiApi(sb.toString(), "gemini-2.0-flash");
    }

    private String buildSystemPrompt(User user, List<TrainingRecord> trainingHistory, List<MealRecord> mealHistory) {
        StringBuilder sb = new StringBuilder();
        
        // 基本役割
        sb.append("あなたはフィットネスアプリ『FitWorks』の専属AIトレーナーです。\n");
        sb.append("トレーニングと食事管理（栄養バランス）の専門家としてチャットをしてください。\n");
        sb.append("ユーザーの要望に合わせて、具体的で効果的なトレーニングメニューや食事のアドバイスを提案してください。\n");
        sb.append("回答はポジティブで親しみやすい口調（日本語）でお願いします。\n\n");
        sb.append("初心者、中級者、上級者別に適したアドバイスを心がけてください。】\n");

        // ユーザー情報
        sb.append("【ユーザー情報】\n");
        sb.append("- 名前: ").append(user.getUsername()).append("\n");
        sb.append("- アプリ利用レベル: Lv.").append(user.getLevel()).append("\n");
        sb.append("※重要: 「アプリ利用レベル」はフィットネス経験値とは異なります。ユーザーがチャットで「初心者」「上級者」などを申告した場合は、その情報を最優先してください。\n\n");
        
        // 食事履歴
        sb.append("【直近の食事履歴】\n");
        if (mealHistory != null && !mealHistory.isEmpty()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd HH:mm");
            for (MealRecord meal : mealHistory) {
                sb.append("- ").append(meal.getMealDateTime().format(dtf))
                  .append(" [").append(meal.getMealType()).append("]: ")
                  .append(meal.getContent());
                if (meal.getCalories() != null) {
                    sb.append(" (").append(meal.getCalories()).append("kcal)");
                }
                sb.append("\n");
            }
        } else {
            sb.append("- 記録なし\n");
        }
        sb.append("\n");

        // トレーニング履歴
        sb.append("【直近のトレーニング履歴】\n");
        if (trainingHistory != null && !trainingHistory.isEmpty()) {
            for (TrainingRecord record : trainingHistory) {
                String menu = "WEIGHT".equals(record.getType()) ? record.getExerciseName() : record.getCardioType();
                sb.append("- ").append(record.getRecordDate()).append(": ").append(menu).append("\n");
            }
        } else {
            sb.append("- 記録なし\n");
        }

        // 回答ルール
        sb.append("\n【回答の絶対ルール（厳守）】\n");
        sb.append("1. テキスト内の強調表示（太字）は一切禁止です。アスタリスク記号は絶対に使わないでください。\n");
        sb.append("2. メニュー名や見出しもプレーンテキストで出力してください。\n");
        sb.append("3. 箇条書きには「・」または「- 」のみを使用してください。\n");
        sb.append("4. 構成は「挨拶」→「回答・提案（箇条書き）」→「一言」の順で、簡潔にまとめてください。\n");
        sb.append("5. 200文字以内で書いてください。\n");
        sb.append("6. 語尾にムキをつけてください。\n");

        return sb.toString();
    }

    private String callGeminiApi(String prompt, String modelName) {
        try {
            Client client = Client.builder()
                .apiKey(apiKey)
                .build();
            
            GenerateContentResponse response = client.models.generateContent(modelName, prompt, null);
            return response.text();

        } catch (Exception e) {
            e.printStackTrace();
            return "通信エラーが発生しました。もう一度試してください。ムキ！";
        }
    }
}