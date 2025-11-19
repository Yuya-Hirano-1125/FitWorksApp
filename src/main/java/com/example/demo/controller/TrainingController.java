package com.example.demo.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
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
    
  private static final Map<String, List<String>> TRAINING_EXERCISES = Map.of( 
          "free-weight", List.of("ベンチプレス", "スクワット", "デッドリフト", "ショルダープレス", "ラットプルダウン", "オーバーヘッドプレス", "ベントオーバーロー", "レッグプレス"), 
          "cardio", List.of("ランニング", "サイクリング", "エリプティカル", "水泳", "ウォーキング", "トレッドミルインターバル", "ローイング") 
      ); 
   
    
    /**
     * トレーニング選択画面 (training.html) を表示
     * @param authentication 認証ユーザー
     * @param model Thymeleafモデル
     * @return training.html
     */
    @GetMapping("/training")
    public String showTrainingOptions(Authentication authentication, Model model) { 
        if (getCurrentUser(authentication) == null) {
            return "redirect:/login"; // ログインしていない場合はログイン画面へ
        }
        
        // 種目リストをモデルに追加
        model.addAttribute("freeWeightExercises", TRAINING_EXERCISES.get("free-weight"));
        model.addAttribute("cardioExercises", TRAINING_EXERCISES.get("cardio"));
        
        return "training"; // src/main/resources/templates/training.htmlをレンダリング
    }

    /**
     * トレーニング種目一覧画面 (exercise-list.html) を表示
     * @param authentication 認証ユーザー
     * @return exercise-list.html
     */
    @GetMapping("/training/exercises")
    public String showExerciseList(Authentication authentication) {
        if (getCurrentUser(authentication) == null) {
            return "redirect:/login"; // ログインしていない場合はログイン画面へ
        }
        return "exercise-list"; // src/main/resources/templates/exercise-list.htmlをレンダリング
    }

    /**
     * トレーニングセッション開始 (training/start) を処理し、セッション画面へ遷移
     * training.htmlからPOSTされたフォームデータを受け取ります。
     * @param type 選択されたトレーニングタイプ
     * @param exerciseName 選択または入力された種目名
     * @param authentication 認証ユーザー
     * @param model Thymeleafモデル
     * @return training-session.html または リダイレクト
     */
    @PostMapping("/training/start") // HTTPメソッドをPOSTに変更
    public String startTrainingSession(
            @RequestParam("type") String type,
            @RequestParam(value = "exerciseName", required = false) String exerciseName,
            Authentication authentication,
            Model model) {
        
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login"; // ログインしていない場合はログイン画面へ
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
                // 選択された種目名、または自由記入された種目名を使用
                if (exerciseName != null && !exerciseName.trim().isEmpty()) {
                    selectedExercise = exerciseName.trim();
                } else {
                    // 種目が選択/入力されていない場合はエラーとして扱う
                    return "redirect:/training"; 
                }
                title = ("free-weight".equals(type) ? "フリーウェイト" : "有酸素運動") + "セッション";
                break;
            default:
                return "redirect:/training"; 
        }
        
        model.addAttribute("trainingType", type);
        model.addAttribute("trainingTitle", title);
        model.addAttribute("selectedExercise", selectedExercise); // 種目名をセッション画面へ渡す
        
        // 実際のトレーニングセッション画面へ遷移 (training-session.htmlが存在することを想定)
        return "training-session"; 
    }
    
    /**
     * トレーニングログ（カレンダー）画面を表示
     * @param authentication 認証ユーザー
     * @param year 表示する年
     * @param month 表示する月
     * @param model Thymeleafモデル
     * @return training-log.html
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

        // データベースから該当月の記録を取得し、日付と記録の有無をMapに変換
        List<TrainingRecord> records = trainingRecordRepository.findByUser_IdAndRecordDateBetween(
                currentUser.getId(), firstOfMonth, lastOfMonth);
        
        Map<LocalDate, Boolean> loggedDates = records.stream()
                .collect(Collectors.toMap(
                    TrainingRecord::getRecordDate,
                    r -> true,
                    (a, b) -> a // 既にキーが存在する場合は上書きしない
                ));

        // カレンダーグリッドの生成
        List<LocalDate> calendarDays = new ArrayList<>();
        
        // 1週目の開始曜日までの空白
        int paddingDays = firstOfMonth.getDayOfWeek().getValue() % 7; 
        if (paddingDays == 0) paddingDays = 7; // 日曜日を0ではなく7として扱う（カレンダー表示のため）
        paddingDays = (paddingDays == 7) ? 0 : paddingDays; // 日曜日を0に戻す

        for (int i = 0; i < paddingDays; i++) {
            calendarDays.add(null); // nullで空白セルを表す
        }

        // 今月の日付
        for (int i = 1; i <= targetYearMonth.lengthOfMonth(); i++) {
            calendarDays.add(targetYearMonth.atDay(i));
        }
        
        // モデルにデータを格納
        model.addAttribute("currentDate", today);
        model.addAttribute("currentYearMonth", targetYearMonth);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("loggedDates", loggedDates);
        model.addAttribute("username", currentUser.getUsername());
        
        // 月のナビゲーションデータ
        model.addAttribute("prevYear", targetYearMonth.minusMonths(1).getYear());
        model.addAttribute("prevMonth", targetYearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("nextYear", targetYearMonth.plusMonths(1).getYear());
        model.addAttribute("nextMonth", targetYearMonth.plusMonths(1).getMonthValue());

        // 曜日ヘッダー
        List<String> dayLabels = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            dayLabels.add(day.getDisplayName(TextStyle.SHORT, Locale.JAPANESE));
        }
        model.addAttribute("dayLabels", dayLabels);
        return "training-log";
    }

    /**
     * フリーウェイトの記録フォームを返す (モーダル内で使用)
     */
    @GetMapping("/training-log/form/weight")
    public String showWeightLogForm(@RequestParam("date") LocalDate date, Model model) {
        TrainingLogForm form = new TrainingLogForm();
        form.setRecordDate(date);
        form.setType("WEIGHT");
        model.addAttribute("trainingLogForm", form);
        return "training-log-form-weight"; 
    }

    /**
     * 有酸素運動の記録フォームを返す (モーダル内で使用)
     */
    @GetMapping("/training-log/form/cardio")
    public String showCardioLogForm(@RequestParam("date") LocalDate date, Model model) {
        TrainingLogForm form = new TrainingLogForm();
        form.setRecordDate(date);
        form.setType("CARDIO");
        model.addAttribute("trainingLogForm", form);
        return "training-log-form-cardio";
    }
    
    /**
     * トレーニング記録を保存する
     */
    @PostMapping("/training-log/save")
    public String saveTrainingRecord(@ModelAttribute("trainingLogForm") TrainingLogForm form, 
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // DTOをEntityにマッピング
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
        
        // 記録した日付を含む月へリダイレクト
        LocalDate recordedDate = form.getRecordDate();
        return "redirect:/training-log?year=" + recordedDate.getYear() + "&month=" + recordedDate.getMonthValue();
    }
}

