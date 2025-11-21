package com.example.demo.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.TrainingLogForm;
import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;

@Controller
public class TrainingController {

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
    @GetMapping("/training")
    public String showTrainingOptions(Authentication authentication, Model model) { 
        if (getCurrentUser(authentication) == null) {
            return "redirect:/login"; 
        }
        
        model.addAttribute("freeWeightExercisesByPart", FREE_WEIGHT_EXERCISES_BY_PART);
        model.addAttribute("freeWeightParts", FREE_WEIGHT_EXERCISES_BY_PART.keySet());
        model.addAttribute("cardioExercises", CARDIO_EXERCISES);
        
        return "training/training"; // ★ 修正
    }

    /**
     * トレーニング種目一覧画面 (exercise-list.html) を表示
     */
    @GetMapping("/training/exercises")
    public String showExerciseList(Authentication authentication) {
        if (getCurrentUser(authentication) == null) {
            return "redirect:/login"; 
        }
        return "training/exercise-list"; // ★ 修正
    }

    /**
     * トレーニングセッション開始
     */
    @PostMapping("/training/start")
    public String startTrainingSession(
            @RequestParam("type") String type,
            @RequestParam(value = "exerciseName", required = false) String exerciseName,
            Authentication authentication,
            Model model) {
        
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login"; 
        }
        
        String title = "";
        String selectedExercise = "";

        switch (type) {
            case "ai-suggested":
                title = "AIおすすめメニューセッション";
                selectedExercise = "AIおすすめプログラム"; 
                break;
            case "free-weight":
            case "cardio":
                if (exerciseName != null && !exerciseName.trim().isEmpty()) {
                    selectedExercise = exerciseName.trim();
                } else {
                    return "redirect:/training"; 
                }
                title = ("free-weight".equals(type) ? "フリーウェイト" : "有酸素運動") + "セッション";
                break;
            default:
                return "redirect:/training"; 
        }
        
        model.addAttribute("trainingType", type);
        model.addAttribute("trainingTitle", title);
        model.addAttribute("selectedExercise", selectedExercise);
        
        // ★ 記録用フォームのために今日の日付を渡す
        model.addAttribute("today", LocalDate.now());
        
        return "training/training-session"; // ★ 修正 (エラーの原因箇所)
    }
    
    /**
     * トレーニングログ（カレンダー）画面を表示
     */
    @GetMapping("/training-log")
    public String showTrainingLog(
            Authentication authentication,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            Model model) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        LocalDate today = LocalDate.now();
        YearMonth targetYearMonth;

        if (year != null && month != null) {
            try {
                targetYearMonth = YearMonth.of(year, month);
            } catch (Exception e) {
                targetYearMonth = YearMonth.from(today);
            }
        } else {
            targetYearMonth = YearMonth.from(today);
        }

        LocalDate firstOfMonth = targetYearMonth.atDay(1);
        LocalDate lastOfMonth = targetYearMonth.atEndOfMonth();

        List<TrainingRecord> records = trainingRecordRepository.findByUser_IdAndRecordDateBetween(
                currentUser.getId(), firstOfMonth, lastOfMonth);
        
        Map<LocalDate, Boolean> loggedDates = records.stream()
                .collect(Collectors.toMap(
                    TrainingRecord::getRecordDate,
                    r -> true,
                    (a, b) -> a 
                ));

        List<LocalDate> calendarDays = new ArrayList<>();
        int paddingDays = firstOfMonth.getDayOfWeek().getValue() % 7; 
        if (paddingDays == 0) paddingDays = 7; 
        paddingDays = (paddingDays == 7) ? 0 : paddingDays; 

        for (int i = 0; i < paddingDays; i++) {
            calendarDays.add(null); 
        }

        for (int i = 1; i <= targetYearMonth.lengthOfMonth(); i++) {
            calendarDays.add(targetYearMonth.atDay(i));
        }
        
        model.addAttribute("currentDate", today);
        model.addAttribute("currentYearMonth", targetYearMonth);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("loggedDates", loggedDates);
        model.addAttribute("username", currentUser.getUsername());
        
        model.addAttribute("prevYear", targetYearMonth.minusMonths(1).getYear());
        model.addAttribute("prevMonth", targetYearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("nextYear", targetYearMonth.plusMonths(1).getYear());
        model.addAttribute("nextMonth", targetYearMonth.plusMonths(1).getMonthValue());

        List<String> dayLabels = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            dayLabels.add(day.getDisplayName(TextStyle.SHORT, Locale.JAPANESE));
        }
        model.addAttribute("dayLabels", dayLabels);
        return "log/training-log"; // ★ 修正
    }

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
        
        return "log/training-log-all"; // ★ 修正
    }

    @GetMapping("/training-log/form/weight")
    public String showWeightLogForm(@RequestParam("date") LocalDate date, Model model) {
        TrainingLogForm form = new TrainingLogForm();
        form.setRecordDate(date);
        form.setType("WEIGHT");
        model.addAttribute("trainingLogForm", form);
        return "log/training-log-form-weight"; // ★ 修正
    }

    @GetMapping("/training-log/form/cardio")
    public String showCardioLogForm(@RequestParam("date") LocalDate date, Model model) {
        TrainingLogForm form = new TrainingLogForm();
        form.setRecordDate(date);
        form.setType("CARDIO");
        model.addAttribute("trainingLogForm", form);
        return "log/training-log-form-cardio"; // ★ 修正
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
        
        if ("WEIGHT".equals(form.getType())) {
            record.setExerciseName(form.getExerciseName());
            record.setSets(form.getSets());
            record.setReps(form.getReps());
            record.setWeight(form.getWeight());
        } else if ("CARDIO".equals(form.getType())) {
            record.setCardioType(form.getCardioType());
            record.setDurationMinutes(form.getDurationMinutes());
            record.setDistanceKm(form.getDistanceKm());
        }

        trainingRecordRepository.save(record);
        
        redirectAttributes.addFlashAttribute("successMessage", form.getRecordDate().toString() + " のトレーニングを記録しました！");
        
        LocalDate recordedDate = form.getRecordDate();
        return "redirect:/training-log?year=" + recordedDate.getYear() + "&month=" + recordedDate.getMonthValue();
    }
}
