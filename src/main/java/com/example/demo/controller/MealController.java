package com.example.demo.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
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
            // AIサービス呼び出し
            String jsonResult = aiCoachService.analyzeMealImage(file);

            // JSONをMapに変換
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> analysisData = mapper.readValue(jsonResult, Map.class);
            
            if (analysisData.containsKey("error")) {
                redirectAttributes.addFlashAttribute("error", analysisData.get("error"));
            } else {
                redirectAttributes.addFlashAttribute("analyzedData", analysisData);
                redirectAttributes.addFlashAttribute("successMessage", "AI解析完了！内容を確認して記録してください。");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "AI解析データの読み込みに失敗しました。");
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
                
                // ★自動補完: 日付と現在時刻をセット
                form.setDate(today.toString());
                LocalTime now = LocalTime.now();
                form.setTime(now.format(DateTimeFormatter.ofPattern("HH:mm")));
                
                // ★自動補完: 時間帯に応じて食事タイプ(朝/昼/夜)をセット
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
            
            // ★AIアドバイス生成処理の修正箇所
            try {
                // 修正: 単なる食事アドバイス(generateMealAdvice)ではなく、トレーニング提案(generateDietBasedTrainingAdvice)を呼び出す
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