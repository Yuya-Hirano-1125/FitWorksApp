package com.example.demo.service;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.MealLogForm;
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

    private Client client;

    // 起動時に一度だけクライアントを初期化（高速化）
    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            try {
                this.client = Client.builder()
                    .apiKey(apiKey)
                    .build();
            } catch (Exception e) {
                System.err.println("Gemini Client Init Error: " + e.getMessage());
            }
        }
    }

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
     * 食事記録に対するワンポイントアドバイスを生成する
     */
    public String generateMealAdvice(User user, MealLogForm form) {
        StringBuilder sb = new StringBuilder();
        sb.append("あなたは栄養管理の専門家AIです。");
        sb.append("ユーザーが食事を記録しました。この食事内容に対して、栄養バランスの観点から短く褒める、またはアドバイスをしてください。\n");
        sb.append("【ユーザー】").append(user.getUsername()).append("さん\n");
        sb.append("【食事内容】").append(form.getContent()).append("\n");
        sb.append("【カロリー】").append(form.getCalories()).append("kcal\n");
        sb.append("【PFC】P:").append(form.getProtein()).append("g, F:").append(form.getFat()).append("g, C:").append(form.getCarbohydrate()).append("g\n");
        sb.append("\nルール: 100文字以内。親しみやすい口調で。絵文字(🥗🍎など)を使って。語尾にムキをつけてください。冒頭の挨拶は不要です。");
        return callGeminiApi(sb.toString());
    }
    
    /**
     * 食事内容に基づいたトレーニング提案
     */
    public String generateDietBasedTrainingAdvice(User user, MealLogForm mealForm) {
        StringBuilder sb = new StringBuilder();
        sb.append("あなたは専属AIトレーナーです。\n");
        sb.append("ユーザーがたった今食事を摂りました。この食事内容と栄養バランスに基づき、直後に行うべき最適なアクションや、次のトレーニングメニューを提案してください。\n\n");
        
        sb.append("【ユーザー】").append(user.getUsername()).append("さん\n");
        sb.append("【摂取した食事】\n");
        sb.append("- 内容: ").append(mealForm.getContent()).append("\n");
        sb.append("- カロリー: ").append(mealForm.getCalories()).append("kcal\n");
        sb.append("- PFCバランス: P(タンパク質):").append(mealForm.getProtein())
          .append("g, F(脂質):").append(mealForm.getFat())
          .append("g, C(炭水化物):").append(mealForm.getCarbohydrate()).append("g\n");

        sb.append("\n【判断基準】\n");
        sb.append("- 炭水化物が多い場合: 血糖値上昇を抑えるための軽いスクワットや、エネルギーを活用した高強度トレーニングを提案。\n");
        sb.append("- タンパク質が多い場合: 筋合成を促すための筋トレメニューを推奨。\n");
        sb.append("- 脂質/カロリー過多の場合: 脂肪燃焼効果の高い有酸素運動やHIITを提案。\n");
        
        sb.append("\nルール: 150文字以内。ポジティブに。「食べたことは悪くない、ここからどう動くかだ！」というスタンスで。語尾にムキをつける。");
        
        return callGeminiApi(sb.toString());
    }

    /**
     * コンディショニング・ケア提案
     */
    public String generateConditioningAdvice(User user, String conditionType) {
        StringBuilder sb = new StringBuilder();
        sb.append("あなたはユーザーの体を気遣うコンディショニング専門のAIトレーナーです。\n");
        sb.append("ユーザーは現在「").append(conditionType).append("」を求めています。\n");
        sb.append("その目的に最適な、具体的かつニッチなケア方法やトレーニングを1つ提案してください。\n\n");
        
        sb.append("【提案の引き出し】\n");
        sb.append("- 眼精疲労: 眼球運動、ホットアイケア、遠近体操\n");
        sb.append("- 全身疲労: 筋膜リリース、交代浴、アクティブレスト\n");
        sb.append("- 心肺機能強化: タバタ式、インターバル走、心拍数管理\n");
        sb.append("- 柔軟性向上: 動的ストレッチ、PNFストレッチ\n");

        sb.append("\nルール: 150文字以内。優しく、かつ専門的に。手順を簡潔に教える。語尾にムキをつける。");

        return callGeminiApi(sb.toString());
    }

    /**
     * ★追加: AIケアアドバイス生成
     */
    public String generateCareAdvice(User user, String symptom, String recommendedExerciseName) {
        StringBuilder sb = new StringBuilder();
        sb.append("あなたはユーザーの体調を気遣う優しいAIトレーナーです。\n");
        sb.append("ユーザーが「").append(symptom).append("」という不調を訴えています。\n");
        sb.append("それに対して、「").append(recommendedExerciseName).append("」というケア方法を提案しました。\n");
        sb.append("ユーザーに対して、労わりの言葉と、そのケアを行う際の簡単なコツを伝えてください。\n\n");
        
        sb.append("【ユーザー情報】\n");
        sb.append("- 名前: ").append(user.getUsername()).append("\n");
        
        sb.append("\nルール: 150文字以内。非常に優しく、リラックスさせるような口調で。ただし語尾には必ず「ムキ」をつけてください（例: リラックスするムキ、無理は禁物ムキ）。");

        return callGeminiApi(sb.toString());
    }

    /**
     * 食事画像を解析して栄養素を推定する
     */
    public String analyzeMealImage(MultipartFile imageFile) {
        try {
            if (this.client == null) return "{\"error\": \"AI機能が有効になっていません\"}";

            String mimeType = imageFile.getContentType();
            if (mimeType == null) mimeType = "image/jpeg";
            byte[] imageBytes = imageFile.getBytes();
            Part imagePart = Part.fromBytes(imageBytes, mimeType);

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

            // ★Gemini 2.0 Flash (試験運用版) を使用
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", content, null);
            
            String responseText = response.text();
            
            // JSONクリーニング処理
            if (responseText.contains("```json")) {
                responseText = responseText.substring(responseText.indexOf("```json") + 7);
                if (responseText.contains("```")) {
                    responseText = responseText.substring(0, responseText.indexOf("```"));
                }
            } else if (responseText.contains("```")) {
                responseText = responseText.replace("```", "");
            }
            
            return responseText.trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"AI解析に失敗しました: " + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    private String buildSystemPrompt(User user, List<TrainingRecord> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("あなたはフィットネスアプリ『FitWorks』の専属AIトレーナーです。\n");
        sb.append("ユーザーの要望に合わせて、具体的で効果的なトレーニングメニューを提案してください。\n");
        sb.append("ユーザーが「疲れた」「目が痛い」と言った場合は、無理に筋トレを勧めず、ストレッチや眼球運動などのケアを提案できる柔軟性を持ってください。\n"); 
        sb.append("回答は熱血かつポジティブな口調（日本語）でお願いします。\n\n");
        
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
        sb.append("3. 熱血かつポジティブに。絵文字(💪🔥など)を多用して。\n");
        sb.append("4. 語尾にムキをつけてください。\n");
        sb.append("5. メニューを提案する際は、会話文とは明確に区別し、箇条書き（行頭に - をつける）で出力してください。\n");

        return sb.toString();
    }

    private String callGeminiApi(String prompt) {
        try {
            if (this.client == null) return "API Key未設定ムキ！";
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "エラーが発生しましたムキ！";
        }
    }
}