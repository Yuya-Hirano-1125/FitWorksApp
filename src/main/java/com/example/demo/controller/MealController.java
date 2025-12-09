package com.example.demo.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters; // ★追加
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.MealLogForm;
import com.example.demo.entity.MealRecord;
import com.example.demo.entity.User;
import com.example.demo.service.AICoachService;
import com.example.demo.service.MealService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/log/meal")
public class MealController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private MealService mealService;
    
    @Autowired
    private AICoachService aiCoachService;

    @PostMapping("/analyze")
    public String analyzeMeal(@RequestParam("mealImage") MultipartFile file,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "画像ファイルが選択されていません。");
            return "redirect:/log/meal";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername());
            
            // AIサービス呼び出し
            String jsonResult = aiCoachService.analyzeMealImage(file);

            // JSONをMapに変換
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> analysisData = mapper.readValue(jsonResult, Map.class);
            
            if (analysisData.containsKey("error")) {
                redirectAttributes.addFlashAttribute("error", analysisData.get("error"));
            } else {
                redirectAttributes.addFlashAttribute("analyzedData", analysisData);
                
                // 解析データを使ってトレーニング提案を生成
                MealLogForm tempForm = new MealLogForm();
                tempForm.setContent((String) analysisData.get("content"));
                tempForm.setCalories(toInteger(analysisData.get("calories")));
                tempForm.setProtein(toDouble(analysisData.get("protein")));
                tempForm.setFat(toDouble(analysisData.get("fat")));
                tempForm.setCarbohydrate(toDouble(analysisData.get("carbohydrate")));
                
                String advice = aiCoachService.generateDietBasedTrainingAdvice(user, tempForm);
                redirectAttributes.addFlashAttribute("aiAdvice", advice);

                redirectAttributes.addFlashAttribute("successMessage", "AI解析＆トレーニング提案完了！内容を確認して記録してください。");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "AI解析データの読み込みに失敗しました。");
        }

        return "redirect:/log/meal";
    }

    // テキストからの解析処理
    @PostMapping("/analyze-text")
    public String analyzeMealText(@ModelAttribute("mealLogForm") MealLogForm form,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            String text = form.getContent();
            if (text == null || text.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "食事内容を入力してください。");
                return "redirect:/log/meal";
            }

            User user = userService.findByUsername(userDetails.getUsername());

            // AIサービス呼び出し（テキスト解析）
            String jsonResult = aiCoachService.analyzeMealText(text);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> analysisData = mapper.readValue(jsonResult, Map.class);
            
            if (analysisData.containsKey("error")) {
                redirectAttributes.addFlashAttribute("error", analysisData.get("error"));
            } else {
                redirectAttributes.addFlashAttribute("analyzedData", analysisData);
                
                // トレーニング提案を生成
                MealLogForm tempForm = new MealLogForm();
                tempForm.setContent((String) analysisData.get("content"));
                tempForm.setCalories(toInteger(analysisData.get("calories")));
                tempForm.setProtein(toDouble(analysisData.get("protein")));
                tempForm.setFat(toDouble(analysisData.get("fat")));
                tempForm.setCarbohydrate(toDouble(analysisData.get("carbohydrate")));
                
                String advice = aiCoachService.generateDietBasedTrainingAdvice(user, tempForm);
                redirectAttributes.addFlashAttribute("aiAdvice", advice);

                redirectAttributes.addFlashAttribute("successMessage", "テキスト解析＆トレーニング提案完了！");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "解析に失敗しました。");
        }
        return "redirect:/log/meal";
    }

    // 月間データの分析処理
    @PostMapping("/analyze-month")
    public String analyzeMonthlyDiet(@RequestParam("year") int year,
                                     @RequestParam("month") int month,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            YearMonth targetYearMonth = YearMonth.of(year, month);
            List<MealRecord> monthlyRecords = mealService.getMonthlyMealRecords(user, targetYearMonth);

            if (monthlyRecords.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", year + "年" + month + "月の記録がありません。");
                return "redirect:/log/meal?year=" + year + "&month=" + month;
            }

            // AIサービス呼び出し
            String advice = aiCoachService.generateMonthlyDietAdvice(user, monthlyRecords, targetYearMonth);
            
            redirectAttributes.addFlashAttribute("aiAdvice", advice);
            redirectAttributes.addFlashAttribute("successMessage", year + "年" + month + "月の分析が完了しました！");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "月間分析中にエラーが発生しました。");
        }
        return "redirect:/log/meal?year=" + year + "&month=" + month;
    }

    // ★追加: 週間データの分析処理 (今週を対象とする)
    @PostMapping("/analyze-week")
    public String analyzeWeeklyDiet(@AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            LocalDate today = LocalDate.now();
            
            // 今週の範囲を決定 (日曜始まり〜土曜終わり)
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

            // 全件取得してフィルタリング (MealServiceに週間取得用メソッドがないため)
            List<MealRecord> allRecords = mealService.getMealRecordsByUser(user);
            List<MealRecord> weeklyRecords = allRecords.stream()
                .filter(r -> {
                    LocalDate d = r.getMealDateTime().toLocalDate();
                    return !d.isBefore(startOfWeek) && !d.isAfter(endOfWeek);
                })
                .collect(Collectors.toList());

            if (weeklyRecords.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "今週(" + startOfWeek + "〜)の記録がありません。");
                return "redirect:/log/meal";
            }

            // AIサービス呼び出し
            String advice = aiCoachService.generateWeeklyDietAdvice(user, weeklyRecords, startOfWeek, endOfWeek);
            
            redirectAttributes.addFlashAttribute("aiAdvice", advice);
            redirectAttributes.addFlashAttribute("successMessage", "今週の分析が完了しました！");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "週間分析中にエラーが発生しました。");
        }
        return "redirect:/log/meal";
    }

    @GetMapping
    public String showMealLogCalendar(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam(value = "year", required = false) Integer year,
                                      @RequestParam(value = "month", required = false) Integer month,
                                      Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        LocalDate today = LocalDate.now();
        YearMonth targetYearMonth;

        if (year != null && month != null) {
            try { targetYearMonth = YearMonth.of(year, month); } catch (Exception e) { targetYearMonth = YearMonth.from(today); }
        } else { targetYearMonth = YearMonth.from(today); }

        List<MealRecord> records = mealService.getMonthlyMealRecords(user, targetYearMonth);
        Map<LocalDate, Boolean> loggedDates = records.stream()
                .collect(Collectors.toMap(r -> r.getMealDateTime().toLocalDate(), r -> true, (e, r) -> e));

        List<LocalDate> calendarDays = new ArrayList<>();
        LocalDate firstOfMonth = targetYearMonth.atDay(1);
        int paddingDays = firstOfMonth.getDayOfWeek().getValue() % 7;
        if (paddingDays == 0) paddingDays = 7; paddingDays = (paddingDays == 7) ? 0 : paddingDays;
        for (int i = 0; i < paddingDays; i++) calendarDays.add(null);
        for (int i = 1; i <= targetYearMonth.lengthOfMonth(); i++) calendarDays.add(targetYearMonth.atDay(i));
        
        List<String> dayLabels = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) dayLabels.add(day.getDisplayName(TextStyle.SHORT, Locale.JAPANESE));

        model.addAttribute("username", user.getUsername());
        model.addAttribute("currentDate", today);
        model.addAttribute("currentYearMonth", targetYearMonth);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("loggedDates", loggedDates);
        model.addAttribute("dayLabels", dayLabels);
        model.addAttribute("prevYear", targetYearMonth.minusMonths(1).getYear());
        model.addAttribute("prevMonth", targetYearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("nextYear", targetYearMonth.plusMonths(1).getYear());
        model.addAttribute("nextMonth", targetYearMonth.plusMonths(1).getMonthValue());

        MealLogForm form = new MealLogForm();
        // AI解析データがある場合、フォームに反映
        if (model.containsAttribute("analyzedData")) {
            Map<String, Object> data = (Map<String, Object>) model.asMap().get("analyzedData");
            if (data != null && !data.containsKey("error")) {
                form.setContent((String) data.get("content"));
                form.setCalories(toInteger(data.get("calories")));
                form.setProtein(toDouble(data.get("protein")));
                form.setFat(toDouble(data.get("fat")));
                form.setCarbohydrate(toDouble(data.get("carbohydrate")));
                model.addAttribute("aiComment", data.get("comment"));
                
                form.setDate(today.toString());
                LocalTime now = LocalTime.now();
                form.setTime(now.format(DateTimeFormatter.ofPattern("HH:mm")));
                
                int hour = now.getHour();
                if (hour >= 4 && hour < 10) form.setMealType("朝食");
                else if (hour >= 10 && hour < 16) form.setMealType("昼食");
                else if (hour >= 16 || hour < 4) form.setMealType("夕食");
            }
        }
        model.addAttribute("mealLogForm", form);

        return "log/meal-log";
    }

    @PostMapping("/save")
    public String saveMealLog(@Valid @ModelAttribute("mealLogForm") MealLogForm form, 
                              BindingResult result, 
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) { 
        
        if (result.hasErrors()) {
            for (ObjectError error : result.getAllErrors()) {
                System.out.println("Validation Error: " + error.getDefaultMessage());
            }
            return "redirect:/log/meal?error";
        }
        
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            MealRecord savedRecord = mealService.saveMealRecord(form, user);
            
            // AIアドバイス生成
            try {
                String advice = aiCoachService.generateDietBasedTrainingAdvice(user, form);
                redirectAttributes.addFlashAttribute("aiAdvice", advice); 
            } catch (Exception e) {
                e.printStackTrace();
            }

            LocalDate date = savedRecord.getMealDateTime().toLocalDate();
            return "redirect:/log/meal?year=" + date.getYear() + "&month=" + date.getMonthValue();
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "保存中にエラーが発生しました。");
            return "redirect:/log/meal";
        }
    }
    
    @GetMapping("/all")
    public String showAllMealLogs(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("mealRecords", mealService.getMealRecordsByUser(user));
        return "log/meal-log-all"; 
    }

    private Integer toInteger(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        return 0;
    }
    private Double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return 0.0;
    }
}