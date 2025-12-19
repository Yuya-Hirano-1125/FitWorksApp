package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.MealLogForm;
import com.example.demo.entity.MealRecord;
import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;
import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

@Service
public class AICoachService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private Client client;

    // gemini-2.5-flash
    private static final String MODEL_ID = "gemini-2.5-flash"; 
    
    // 待機時間をさらに余裕を持たせる (429対策)
    private static final int MAX_RETRIES = 5;
    private static final long MIN_WAIT_MS = 20000; // 最低20秒待機

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
     * ★追加: ホーム画面用の一言アドバイス生成
     * 時間帯や状況に合わせて、柔軟なアドバイス（40文字以内）を生成します。
     * エラー発生時は、AIエラーを表示せず、デフォルトの挨拶を返します。
     */
    public String generateHomeAdvice(User user) {
        LocalTime now = LocalTime.now();
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        String situation;
        
        // 時間帯による状況設定
        if (now.isBefore(LocalTime.of(10, 0))) {
            situation = "朝です。寝起きで体が硬いかもしれません。活動スイッチを入れる提案を。";
        } else if (now.isAfter(LocalTime.of(18, 0))) {
            situation = "夜です。今日一日の仕事や勉強の疲れが溜まっています。リラックスや軽いストレッチを。";
        } else {
            situation = "日中です。活動の合間です。隙間時間の運動や気分転換を。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("あなたは親しみやすい専属AIトレーナーです。\n");
        sb.append("ユーザー【").append(user.getUsername()).append("】さんがアプリのホーム画面を開きました。\n");
        sb.append("現在時刻は").append(timeStr).append("、状況は「").append(situation).append("」です。\n");
        sb.append("ユーザーの疲労度や生活リズムを気遣い、この瞬間に最適な「一言アドバイス」をください。\n\n");
        
        sb.append("【回答ルール】\n");
        sb.append("- 40文字以内で簡潔に。\n");
        sb.append("- 「今日は軽めに」「ガッツリ行こう」「まずは深呼吸」など、柔軟に提案する。\n");
        sb.append("- 語尾に「ムキ」をつける。\n");
        sb.append("- 挨拶は短く、すぐにアドバイスに入る。\n");

        try {
            // エラーハンドリングのため、共通メソッドを使わず直接呼び出し
            if (this.client == null) throw new IllegalStateException("API Key未設定");
            GenerateContentResponse response = client.models.generateContent(MODEL_ID, sb.toString(), null);
            return response.text();
        } catch (Exception e) {
            // ログには出すが、画面には挨拶だけを返す
            System.err.println("Home Advice Error: " + e.getMessage());
            return user.getUsername() + "さん、今日も良い筋肉ライフをムキ！";
        }
    }

    public String generateCoachingAdvice(User user, List<TrainingRecord> history, String userMessage) {
        String systemPrompt = buildSystemPrompt(user, history);
        String fullPrompt = systemPrompt + "\n\nUser Question: " + userMessage;
        return callGeminiApi(fullPrompt); 
    }

    public String generateTrainingAdvice(User user, String trainingSummary) {
        StringBuilder sb = new StringBuilder();
        sb.append("あなたはフィットネスアプリの専属AIトレーナーです。\n");
        sb.append("ユーザーがトレーニングを記録しました。この努力を盛大に褒めて、モチベーションを上げてください。\n");
        sb.append("【ユーザー】").append(user.getUsername()).append("さん\n");
        sb.append("【行ったトレーニング】").append(trainingSummary).append("\n");
        sb.append("\nルール: 100文字以内で簡潔に。熱血かつポジティブに。絵文字(💪🔥など)を多用して。語尾にムキをつけてください。");
        return callGeminiApi(sb.toString());
    }

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

    public String generateMonthlyDietAdvice(User user, List<MealRecord> records, YearMonth yearMonth) {
        int totalCalories = records.stream().mapToInt(MealRecord::getCalories).sum();
        
        String allContent = records.stream()
                .map(MealRecord::getContent)
                .filter(c -> c != null && !c.trim().isEmpty())
                .limit(30)
                .collect(Collectors.joining("、"));
        
        if (allContent.length() > 500) {
            allContent = allContent.substring(0, 500) + "...";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("あなたは専属AIトレーナーです。\n");
        sb.append("ユーザーの").append(yearMonth.getYear()).append("年").append(yearMonth.getMonthValue()).append("月の1ヶ月間の食事記録を分析し、総評と、それに合わせた【おすすめのトレーニング】および【おすすめの食事メニュー】を提案してください。\n\n");
        
        sb.append("【ユーザー】").append(user.getUsername()).append("さん\n");
        sb.append("【月間データ】\n");
        sb.append("- 食べたもの(抜粋): ").append(allContent).append("\n");
        sb.append("- 月間総摂取カロリー: ").append(totalCalories).append("kcal\n");
        sb.append("- 記録回数: ").append(records.size()).append("回\n");
        
        sb.append("\nルール: 300文字以内。食べたものの傾向を分析し、不足栄養素を補う【食事】と、カロリー収支に合わせた【トレーニング】を具体的に提案する。熱血かつポジティブに。語尾にムキをつける。");

        try {
            return generateContentWithRetry(null, sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "分析に失敗しましたムキ...";
        }
    }

    public String generateWeeklyDietAdvice(User user, List<MealRecord> records, LocalDate start, LocalDate end) {
        int totalCalories = records.stream().mapToInt(MealRecord::getCalories).sum();
        double avgCalories = totalCalories / (double) records.size(); 

        String allContent = records.stream()
                .map(MealRecord::getContent)
                .filter(c -> c != null && !c.trim().isEmpty())
                .limit(20)
                .collect(Collectors.joining("、"));

        StringBuilder sb = new StringBuilder();
        sb.append("あなたは専属AIトレーナーです。\n");
        sb.append("今週(").append(start).append("～").append(end).append(")の食事記録を分析し、週末や来週に向けた【食事】と【トレーニング】のアクションプランを提案してください。\n\n");
        
        sb.append("【ユーザー】").append(user.getUsername()).append("さん\n");
        sb.append("【週間データ】\n");
        sb.append("- 主な食事: ").append(allContent).append("\n");
        sb.append("- 合計カロリー: ").append(totalCalories).append("kcal\n");
        sb.append("- 1食平均:約").append((int)avgCalories).append("kcal\n");
        
        sb.append("\nルール: 250文字以内。週単位の振り返りとして、リカバリーのための【食事調整】や、さらに伸ばすための【トレーニング】を提案。熱血かつポジティブに。語尾にムキをつける。");

        try {
            return generateContentWithRetry(null, sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "分析に失敗しましたムキ...";
        }
    }

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

            return generateContentWithRetry(content, null);

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"AI解析に失敗しました: " + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    public String analyzeMealText(String textDescription) {
        try {
            if (this.client == null) return "{\"error\": \"AI機能が有効になっていません\"}";

            String promptText = """
                以下の食事内容の説明文から、栄養素を推定してください。
                入力テキスト: "%s"

                以下の情報をJSON形式で出力してください。Markdownのコードブロックは不要です。純粋なJSON文字列のみを返してください。
                推測で構わないので、必ず数値を埋めてください。

                {
                    "content": "料理名（日本語で整理して）",
                    "calories": カロリー(整数),
                    "protein": タンパク質g(数値),
                    "fat": 脂質g(数値),
                    "carbohydrate": 炭水化物g(数値),
                    "comment": "AIからの短いコメント（50文字以内）"
                }
                """.formatted(textDescription);

            return generateContentWithRetry(null, promptText);

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"テキスト解析に失敗しました\"}";
        }
    }

    private String generateContentWithRetry(Content content, String promptText) throws Exception {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                GenerateContentResponse response;
                if (content != null) {
                    response = client.models.generateContent(MODEL_ID, content, null);
                } else {
                    response = client.models.generateContent(MODEL_ID, promptText, null);
                }
                return cleanJsonResult(response.text());

            } catch (ClientException e) {
                if (e.getMessage().contains("429") || e.getMessage().contains("Quota exceeded") || 
                    e.getMessage().contains("503") || e.getMessage().contains("404")) {
                    
                    lastException = e;
                    attempt++;
                    long waitTime = MIN_WAIT_MS; 
                    Matcher matcher = Pattern.compile("retry in ([0-9\\.]+)s").matcher(e.getMessage());
                    if (matcher.find()) {
                        try {
                            double seconds = Double.parseDouble(matcher.group(1));
                            waitTime = (long) (seconds * 1000) + 3000; 
                        } catch (NumberFormatException nfe) {}
                    } else {
                         waitTime = MIN_WAIT_MS * attempt;
                    }
                    System.out.println("Gemini API Error (" + e.getMessage() + "). Retrying in " + waitTime + "ms... (" + attempt + "/" + MAX_RETRIES + ")");
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new Exception("解析処理が中断されました。");
                        }
                    }
                } else {
                    throw e; 
                }
            }
        }
        System.err.println("Gemini API Retry Failed: " + lastException.getMessage());
        return "{\"error\": \"現在アクセスが集中しており解析できません。1分ほど待ってから再試行してください。\"}";
    }

    private String cleanJsonResult(String responseText) {
        if (responseText == null) return "{}";
        String cleaned = responseText.trim();
        if (cleaned.contains("```json")) {
            cleaned = cleaned.substring(cleaned.indexOf("```json") + 7);
            if (cleaned.contains("```")) {
                cleaned = cleaned.substring(0, cleaned.indexOf("```"));
            }
        } else if (cleaned.contains("```")) {
            cleaned = cleaned.replace("```", "");
        }
        return cleaned.trim();
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
            GenerateContentResponse response = client.models.generateContent(MODEL_ID, prompt, null);
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            // エラー内容はログに出すが、画面には短いメッセージだけを返す
            return "アクセスが集中しているムキ！少し時間を置いて試してほしいムキ！";
        }
    }
}