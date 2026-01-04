package com.example.demo.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AICoachService;
import com.example.demo.service.LevelService;
import com.example.demo.service.MealService;
import com.example.demo.service.MissionService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/log/meal")
public class MealController {

    @Autowired
    private MealService mealService;
    @Autowired
    private AICoachService aiCoachService;
    @Autowired
    private UserService userService;
    
    @Autowired
    private LevelService levelService;
    @Autowired
    private MissionService missionService;
    @Autowired
    private UserRepository userRepository;

    // カレンダー表示用の共通データ作成メソッド
    private void prepareCalendarData(Model model, User user, Integer year, Integer month) {
        LocalDate today = LocalDate.now();
        YearMonth targetYearMonth;
        if (year != null && month != null) {
            targetYearMonth = YearMonth.of(year, month);
        } else {
            targetYearMonth = YearMonth.from(today);
        }
        
        LocalDate firstOfMonth = targetYearMonth.atDay(1);
        List<LocalDate> calendarDays = new ArrayList<>();
        int paddingDays = firstOfMonth.getDayOfWeek().getValue() % 7; 
        for (int i = 0; i < paddingDays; i++) {
            calendarDays.add(null);
        }
        for (int i = 1; i <= targetYearMonth.lengthOfMonth(); i++) {
            calendarDays.add(targetYearMonth.atDay(i));
        }
        
        List<MealRecord> records = mealService.getMonthlyMealRecords(user, targetYearMonth);
        Map<LocalDate, Boolean> loggedDates = records.stream()
                .map(r -> r.getMealDateTime().toLocalDate())
                .collect(Collectors.toMap(d -> d, d -> true, (a, b) -> a));
        
        YearMonth prev = targetYearMonth.minusMonths(1);
        YearMonth next = targetYearMonth.plusMonths(1);
        
        model.addAttribute("currentDate", today);
        model.addAttribute("currentYearMonth", targetYearMonth);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("loggedDates", loggedDates);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("prevYear", prev.getYear());
        model.addAttribute("prevMonth", prev.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());
        model.addAttribute("dayLabels", Arrays.asList("日", "月", "火", "水", "木", "金", "土"));
    }

    @GetMapping
    public String showMealLogForm(@RequestParam(name = "year", required = false) Integer year,
                                  @RequestParam(name = "month", required = false) Integer month,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        prepareCalendarData(model, user, year, month);

        MealLogForm form = new MealLogForm();
        form.setDate(LocalDate.now().toString());
        form.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        
        model.addAttribute("mealLogForm", form);
        
        return "log/meal-log";
    }

    @PostMapping("/save")
    public String saveMealLog(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute MealLogForm form,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "入力内容に不備があります。");
            return "redirect:/log/meal"; 
        }

        try {
            MultipartFile imageFile = form.getImageFile();
            if (imageFile != null && !imageFile.isEmpty()) {
                // 画像保存処理（必要なら実装）
            }

            MealRecord savedRecord = mealService.saveMealRecord(form, user);

            // 報酬付与
            int rewardXp = 50;
            int rewardCoins = 10;
            levelService.addXpAndCheckLevelUp(user, rewardXp);
            userService.addChips(user.getUsername(), rewardCoins);
            userRepository.save(user);

            missionService.updateMissionProgress(user.getId(), "MEAL_LOG");

            try {
                String advice = aiCoachService.generateMealAdvice(user, form);
                redirectAttributes.addFlashAttribute("aiAdvice", advice);
            } catch (Exception e) {
                System.out.println("AI Advice Error: " + e.getMessage());
            }

            redirectAttributes.addFlashAttribute("successMessage", "食事を記録しました！ (+" + rewardXp + " XP, +" + rewardCoins + " コイン)");
            
            LocalDate date = savedRecord.getMealDateTime().toLocalDate();
            return "redirect:/log/meal?year=" + date.getYear() + "&month=" + date.getMonthValue();
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "保存中にエラーが発生しました。");
            return "redirect:/log/meal";
        }
    }
    
    // ★追加: 月間分析
    @PostMapping("/analyze-month")
    public String analyzeMonth(@RequestParam("year") int year, 
                               @RequestParam("month") int month,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        YearMonth ym = YearMonth.of(year, month);
        List<MealRecord> records = mealService.getMonthlyMealRecords(user, ym);
        
        if (records.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "指定された月の食事記録がありません。");
        } else {
            String advice = aiCoachService.generateMonthlyDietAdvice(user, records, ym);
            redirectAttributes.addFlashAttribute("aiAdvice", advice);
        }
        return "redirect:/log/meal?year=" + year + "&month=" + month;
    }

    // ★追加: 今週の分析
    @PostMapping("/analyze-week")
    public String analyzeWeek(@AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        
        LocalDate today = LocalDate.now();
        LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        List<MealRecord> records = mealService.getMealRecordsBetween(user, start, end);
        
        if (records.isEmpty()) {
             redirectAttributes.addFlashAttribute("error", "今週の食事記録がありません。");
        } else {
             String advice = aiCoachService.generateWeeklyDietAdvice(user, records, start, end);
             redirectAttributes.addFlashAttribute("aiAdvice", advice);
        }
        
        return "redirect:/log/meal";
    }

    // ★追加: 画像解析
    @PostMapping("/analyze")
    public String analyzeImage(@RequestParam("mealImage") MultipartFile file,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        prepareCalendarData(model, user, LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        
        MealLogForm form = new MealLogForm();
        form.setDate(LocalDate.now().toString());
        form.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        try {
            String jsonResult = aiCoachService.analyzeMealImage(file);
            fillFormFromJson(form, jsonResult, model);
        } catch (Exception e) {
            model.addAttribute("error", "画像解析に失敗しました: " + e.getMessage());
        }
        
        model.addAttribute("mealLogForm", form);
        return "log/meal-log";
    }

    // ★追加: テキスト解析
    @PostMapping("/analyze-text")
    public String analyzeText(@ModelAttribute MealLogForm form,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        
        // フォームの日付からカレンダー年月を復元
        LocalDate date = LocalDate.parse(form.getDate());
        prepareCalendarData(model, user, date.getYear(), date.getMonthValue());

        try {
            String jsonResult = aiCoachService.analyzeMealText(form.getContent());
            fillFormFromJson(form, jsonResult, model);
        } catch (Exception e) {
            model.addAttribute("error", "テキスト解析に失敗しました: " + e.getMessage());
        }
        
        model.addAttribute("mealLogForm", form);
        return "log/meal-log";
    }

    // JSONデータをフォームに反映するヘルパー
    private void fillFormFromJson(MealLogForm form, String jsonResult, Model model) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonResult);
            
            if (node.has("error")) {
                model.addAttribute("error", node.get("error").asText());
                return;
            }

            if (node.has("content")) form.setContent(node.get("content").asText());
            if (node.has("calories")) form.setCalories(node.get("calories").asInt());
            if (node.has("protein")) form.setProtein(node.get("protein").asDouble());
            if (node.has("fat")) form.setFat(node.get("fat").asDouble());
            if (node.has("carbohydrate")) form.setCarbohydrate(node.get("carbohydrate").asDouble());
            
            if (node.has("comment")) {
                model.addAttribute("aiComment", node.get("comment").asText());
            }
        } catch (Exception e) {
            model.addAttribute("error", "解析結果の読み込みに失敗しました。");
        }
    }
    
    @GetMapping("/all")
    public String showAllMealLogs(@AuthenticationPrincipal UserDetails userDetails, 
                                  @RequestParam(value = "mealType", required = false) String mealType,
                                  Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        
        List<MealRecord> allRecords = mealService.getMealRecordsByUser(user);
        
        if (mealType != null && !mealType.isEmpty()) {
            allRecords = allRecords.stream()
                    .filter(r -> mealType.equals(r.getMealType()))
                    .collect(Collectors.toList());
        }
        
        model.addAttribute("mealRecords", allRecords);
        model.addAttribute("selectedMealType", mealType);
        
        return "log/meal-log-all"; 
    }
}