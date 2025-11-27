package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;

@Service
public class AICoachService {

    @Value("${gemini.api.key}") // application.propertiesなどで設定されていると仮定
    private String apiKey;

    // ★ メインのメソッド: ユーザー情報を含めた回答を生成
    public String generateCoachingAdvice(User user, List<TrainingRecord> history, String userMessage) {
        
        // 1. システムプロンプト（AIへの役割指示）の構築
        String systemPrompt = buildSystemPrompt(user, history);

        // 2. APIリクエストの作成と送信 (実際の実装に合わせて修正してください)
        // ここでは、プロンプトとユーザーメッセージを結合して送信するイメージです
        String fullPrompt = systemPrompt + "\n\nUser Question: " + userMessage;
        
        return callGeminiApi(fullPrompt); 
    }

    // ★ プロンプト構築ロジック
    private String buildSystemPrompt(User user, List<TrainingRecord> history) {
        StringBuilder sb = new StringBuilder();
        
        // 基本役割
        sb.append("あなたはフィットネスアプリ『FitWorks』の専属AIトレーナーです。\n");
        sb.append("ユーザーの目標、経験レベル、体調、利用可能時間に合わせて、具体的で効果的なトレーニングメニューを提案してください。\n");
        sb.append("回答は励ますような、ポジティブで親しみやすい口調（日本語）でお願いします。\n\n");

        // ユーザー情報
        String levelLabel = determineLevelLabel(user.getLevel());
        sb.append("【ユーザー情報】\n");
        sb.append("- 名前: ").append(user.getUsername()).append("\n");
        sb.append("- 現在のレベル: Lv.").append(user.getLevel()).append(" (").append(levelLabel).append(")\n");
        
        // トレーニング履歴
        sb.append("【直近のトレーニング履歴 (重複部位を避ける参考にしてください)】\n");
        if (history != null && !history.isEmpty()) {
            for (TrainingRecord record : history) {
                String menu = "WEIGHT".equals(record.getType()) ? record.getExerciseName() : record.getCardioType();
                sb.append("- ").append(record.getRecordDate()).append(": ").append(menu).append("\n");
            }
        } else {
            sb.append("- 記録なし（初心者、または久しぶりのトレーニングの可能性があります）\n");
        }

        // 提案のルール
        sb.append("\n【提案時のルール】\n");
        sb.append("1. ユーザーが部位（胸、背中など）を指定した場合は、それに特化したメニューを提案してください。\n");
        sb.append("2. 時間（30分など）が指定された場合、その時間内で終わるセット数・休憩時間を考慮してください。\n");
        sb.append("3. 直近で鍛えた部位が疲労している可能性がある場合は、別の部位を提案するか、確認してください。\n");
        sb.append("4. 初心者にはフォームの注意点を、上級者には高強度テクニック（スーパーセットなど）を含めても良いです。\n");

        return sb.toString();
    }

    // レベルから「初心者/中級者/上級者」を判定する簡易ロジック
    private String determineLevelLabel(int level) {
        if (level <= 10) return "初心者";
        if (level <= 30) return "初中級者";
        if (level <= 50) return "中級者";
        return "上級者";
    }

    // 実際のAPI呼び出し部分（既存の実装を使用してください）
    private String callGeminiApi(String prompt) {
        // ... (既存のGemini API呼び出しコード) ...
        // 例: WebClientやRestTemplateを使ってGoogleのAPIを叩く処理
        // ここではダミーレスポンスを返します
        return "（API連携処理がここに記述されます）AIからの回答: " + prompt; 
    }
}