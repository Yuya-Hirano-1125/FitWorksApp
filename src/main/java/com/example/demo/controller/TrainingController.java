








package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; 

@Controller
public class TrainingController {

<<<<<<< HEAD
    @Autowired
    private UserService userService; 

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TrainingRecordRepository trainingRecordRepository;

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userService.findByUsername(authentication.getName());
    }
// ★★★ ここにメソッドと定数を追加する ★★★
    
    // 経験値(XP)の定義
    private static final int XP_BEGINNER = 300;  // 初級: 300 XP
    private static final int XP_INTERMEDIATE = 500; // 中級: 500 XP
    private static final int XP_ADVANCED = 1000; // 上級: 1000 XP
    private static final int XP_PER_LEVEL = 5000; // レベルアップに必要なXP

    /**
     * 種目名から難易度（XP）を取得するヘルパーメソッド
     */
    private int getExperiencePoints(String exerciseName) {
        if (exerciseName == null || exerciseName.trim().isEmpty()) {
            return 0; 
        }
        
        // 種目名に難易度ラベルが含まれているかチェックする
        if (exerciseName.contains("(上級)")) {
            return XP_ADVANCED;
        } else if (exerciseName.contains("(中級)")) {
            return XP_INTERMEDIATE;
        } else if (exerciseName.contains("(初級)")) {
            return XP_BEGINNER;
        }
        // ラベルが見つからない場合は0を返す
        return 0; 
    }
    private static final Map<String, List<String>> FREE_WEIGHT_EXERCISES_BY_PART = new LinkedHashMap<>() {{
        // 初級: チェストフライ
        // 中級: ベンチプレス, ダンベルプレス, インクラインプレス
        put("胸", List.of(
            "チェストフライ (初級)", 
            "ベンチプレス (中級)", 
            "ダンベルプレス (中級)", 
            "インクラインプレス (中級)"
        ));
        
        // 初級: ラットプルダウン, シーテッドロー
        // 中級: ベントオーバーロー
        // 上級: デッドリフト
        put("背中", List.of(
            "ラットプルダウン (初級)", 
            "シーテッドロー (初級)", 
            "ベントオーバーロー (中級)", 
            "デッドリフト (上級)"
        ));
        
        // 初級: レッグプレス, レッグエクステンション, レッグカール
        // 中級: スクワット
        put("脚", List.of(
            "レッグプレス (初級)", 
            "レッグエクステンション (初級)", 
            "レッグカール (初級)", 
            "スクワット (中級)"
        ));
        
        // 初級: サイドレイズ, フロントレイズ
        // 中級: ショルダープレス, オーバーヘッドプレス
        put("肩", List.of(
            "サイドレイズ (初級)", 
            "フロントレイズ (初級)", 
            "ショルダープレス (中級)", 
            "オーバーヘッドプレス (中級)"
        ));
        
        // 初級: アームカール, ハンマーカール, トライセプスエクステンション
        put("腕", List.of(
            "アームカール (初級)", 
            "ハンマーカール (初級)", 
            "トライセプスエクステンション (初級)"
        ));
        
        // 初級: クランチ
        // 中級: レッグレイズ, ロシアンツイスト
        put("腹筋", List.of(
            "クランチ (初級)", 
            "レッグレイズ (中級)", 
            "ロシアンツイスト (中級)"
        ));
        
        // 初級: カーフレイズ
        // 中級: ヒップスラスト
        put("その他", List.of(
            "カーフレイズ (初級)", 
            "ヒップスラスト (中級)"
        ));
    }};
    
    // 【有酸素運動リストを難易度順・ラベル付きで定義】
    private static final List<String> CARDIO_EXERCISES = List.of(
            "ウォーキング (初級)", 
            "サイクリング (初級)", 
            "エリプティカル (初級)", 
            "ランニング (中級)", 
            "水泳 (中級)", 
            "ローイング (中級)", 
            "トレッドミルインターバル (上級)"
        );
    
    /**
     * トレーニング選択画面 (training.html) を表示
     */
=======
    // ★ 修正点: /training のメイン画面ルーティングをAuthControllerから引き継ぐ
>>>>>>> branch 'master' of https://github.com/Yuya-Hirano-1125/FitWorksApp.git
    @GetMapping("/training")
    public String showTrainingOptions(Model model) {
        // training.html は特別なモデルデータなしでレンダリングされます
        return "training";
    }

    /**
     * トレーニング開始時の各オプションを処理し、入力画面に遷移します。
     * @param type 選択されたトレーニングタイプ (ai-suggested, free-weight, cardio)
     */
    @GetMapping("/training/start")
    public String startTraining(@RequestParam("type") String type, Model model) {
        
        // 仮の種目データ（選択肢用）をモデルに追加
        if (type.equals("free-weight")) {
             model.addAttribute("freeWeightExercises", List.of("ベンチプレス", "スクワット", "デッドリフト"));
        } else if (type.equals("cardio")) {
             model.addAttribute("cardioExercises", List.of("ランニング", "サイクリング", "水泳"));
        }
        
        model.addAttribute("trainingType", type);
        model.addAttribute("trainingTitle", "トレーニング記録");

        switch (type) {
            case "ai-suggested":
                model.addAttribute("selectedExercise", "AIおすすめメニュー");
                model.addAttribute("programName", "腹筋をバキバキにするプログラム");
                return "training-session";
                
            case "free-weight":
                model.addAttribute("selectedExercise", "フリーウェイト");
                return "training-form-weight";
                
            case "cardio":
                model.addAttribute("selectedExercise", "有酸素運動");
                return "training-form-cardio";

            default:
                return "redirect:/training"; 
        }
    }

<<<<<<< HEAD
    /**
     * ★ 追加: 全トレーニング記録一覧画面を表示
     */
    @GetMapping("/training-log/all")
    public String showAllTrainingLog(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 全記録を取得してモデルに追加
        List<TrainingRecord> allRecords = trainingRecordRepository.findByUser_IdOrderByRecordDateDesc(currentUser.getId());
        model.addAttribute("records", allRecords);
        
        return "training-log-all";
    }

    @GetMapping("/training-log/form/weight")
    public String showWeightLogForm(@RequestParam("date") LocalDate date, Model model) {
        TrainingLogForm form = new TrainingLogForm();
        form.setRecordDate(date);
        form.setType("WEIGHT");
        model.addAttribute("trainingLogForm", form);
        return "training-log-form-weight"; 
    }

    @GetMapping("/training-log/form/cardio")
    public String showCardioLogForm(@RequestParam("date") LocalDate date, Model model) {
        TrainingLogForm form = new TrainingLogForm();
        form.setRecordDate(date);
        form.setType("CARDIO");
        model.addAttribute("trainingLogForm", form);
        return "training-log-form-cardio";
    }
    
    @PostMapping("/training-log/save")
    public String saveTrainingRecord(@ModelAttribute("trainingLogForm") TrainingLogForm form, 
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        TrainingRecord record = new TrainingRecord();
        record.setUser(currentUser);
        record.setRecordDate(form.getRecordDate());
        record.setType(form.getType());
        
        String exerciseIdentifier = null;

        if ("WEIGHT".equals(form.getType())) {
            record.setExerciseName(form.getExerciseName());
            record.setSets(form.getSets());
            record.setReps(form.getReps());
            record.setWeight(form.getWeight());
            exerciseIdentifier = form.getExerciseName();
        } else if ("CARDIO".equals(form.getType())) {
            record.setCardioType(form.getCardioType());
            record.setDurationMinutes(form.getDurationMinutes());
            record.setDistanceKm(form.getDistanceKm());
            exerciseIdentifier = form.getCardioType();
        }

        trainingRecordRepository.save(record);
        
        // ★★★ ここからXP更新ロジックの追加 ★★★
        int earnedXP = 0;
        
        if (exerciseIdentifier != null) {
            earnedXP = getExperiencePoints(exerciseIdentifier);
        }
        
        if (earnedXP > 0) {
            int newTotalXp = currentUser.getXp() + earnedXP;
            currentUser.setXp(newTotalXp);
            userRepository.save(currentUser); // DBにユーザーXPを保存
            
            redirectAttributes.addFlashAttribute("successMessage", 
                form.getRecordDate().toString() + " のトレーニングを記録し、" + earnedXP + " XPを獲得しました！");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", form.getRecordDate().toString() + " のトレーニングを記録しました！");
        }
        // ★★★ ここまでXP更新ロジックの追加 ★★★
        
        LocalDate recordedDate = form.getRecordDate();
        return "redirect:/training-log?year=" + recordedDate.getYear() + "&month=" + recordedDate.getMonthValue();
    }
=======
    // TODO: @PostMapping("/training/save") で記録をDBに保存するメソッドを後で追加する
>>>>>>> branch 'master' of https://github.com/Yuya-Hirano-1125/FitWorksApp.git
}