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

import com.example.demo.dto.MealLogForm;
import com.example.demo.entity.MealRecord;
import com.example.demo.entity.User;
import com.example.demo.service.MealService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/log/meal")
public class MealController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private MealService mealService;

    // ... (showMealLogCalendar メソッドはそのまま維持) ...
    @GetMapping
    public String showMealLogCalendar(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam(value = "year", required = false) Integer year,
                                      @RequestParam(value = "month", required = false) Integer month,
                                      Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
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

        List<MealRecord> records = mealService.getMonthlyMealRecords(user, targetYearMonth);

        Map<LocalDate, Boolean> loggedDates = records.stream()
                .collect(Collectors.toMap(
                    record -> record.getMealDateTime().toLocalDate(),
                    r -> true,
                    (existing, replacement) -> existing
                ));

        List<LocalDate> calendarDays = new ArrayList<>();
        LocalDate firstOfMonth = targetYearMonth.atDay(1);
        
        int paddingDays = firstOfMonth.getDayOfWeek().getValue() % 7; 
        if (paddingDays == 0) paddingDays = 7;
        paddingDays = (paddingDays == 7) ? 0 : paddingDays;

        for (int i = 0; i < paddingDays; i++) {
            calendarDays.add(null);
        }
        for (int i = 1; i <= targetYearMonth.lengthOfMonth(); i++) {
            calendarDays.add(targetYearMonth.atDay(i));
        }
        
        List<String> dayLabels = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            dayLabels.add(day.getDisplayName(TextStyle.SHORT, Locale.JAPANESE));
        }

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

        // ★フォーム用オブジェクトをカレンダー画面に渡す
        model.addAttribute("mealLogForm", new MealLogForm());

        return "log/meal-log";
    }

    // ★ 削除: showMealLogForm メソッドは不要になったので消してください

    @PostMapping("/save")
    public String saveMealLog(@Valid @ModelAttribute("mealLogForm") MealLogForm form, 
                              BindingResult result, 
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        
        if (result.hasErrors()) {
            // ★変更: エラー時はカレンダー画面にリダイレクト（簡易対応）
            // 元の meal-log-form は存在しないため、そこには戻せません
            return "redirect:/log/meal"; 
        }

        User user = userService.findByUsername(userDetails.getUsername());
        MealRecord savedRecord = mealService.saveMealRecord(form, user);

        LocalDate date = savedRecord.getMealDateTime().toLocalDate();
        return "redirect:/log/meal?year=" + date.getYear() + "&month=" + date.getMonthValue();
    }
    
    // ... (showAllMealLogs メソッドはそのまま維持) ...
    @GetMapping("/all")
    public String showAllMealLogs(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<MealRecord> mealRecords = mealService.getMealRecordsByUser(user);
        
        model.addAttribute("mealRecords", mealRecords);
        return "log/meal-log-all"; 
    }
}





























