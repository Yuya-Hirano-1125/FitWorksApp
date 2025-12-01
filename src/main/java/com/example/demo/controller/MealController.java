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

    /** * 食事記録カレンダーの表示 (メイン画面)
     * GET /log/meal 
     */
    @GetMapping
    public String showMealLogCalendar(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam(value = "year", required = false) Integer year,
                                      @RequestParam(value = "month", required = false) Integer month,
                                      Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        
        LocalDate today = LocalDate.now();
        YearMonth targetYearMonth;

        // 年月の指定があればその月を、なければ今月を表示
        if (year != null && month != null) {
            try {
                targetYearMonth = YearMonth.of(year, month);
            } catch (Exception e) {
                targetYearMonth = YearMonth.from(today);
            }
        } else {
            targetYearMonth = YearMonth.from(today);
        }

        // 月のデータを取得
        List<MealRecord> records = mealService.getMonthlyMealRecords(user, targetYearMonth);

        // 記録がある日付のマップを作成
        Map<LocalDate, Boolean> loggedDates = records.stream()
                .collect(Collectors.toMap(
                    record -> record.getMealDateTime().toLocalDate(),
                    r -> true,
                    (existing, replacement) -> existing
                ));

        // カレンダーの日付リスト生成（空白埋め含む）
        List<LocalDate> calendarDays = new ArrayList<>();
        LocalDate firstOfMonth = targetYearMonth.atDay(1);
        
        // 月初めの空白日（日曜日始まりの前の部分）を計算（月曜始まりの場合は調整）
        // ここではTrainingControllerに合わせて月曜始まりと想定のロジックを使用
        int paddingDays = firstOfMonth.getDayOfWeek().getValue() % 7; 
        // もし日曜始まりにするなら: int paddingDays = firstOfMonth.getDayOfWeek().getValue() == 7 ? 0 : firstOfMonth.getDayOfWeek().getValue();
        // トレーニングカレンダーの実装に合わせるため同じロジックを採用
        if (paddingDays == 0) paddingDays = 7;
        paddingDays = (paddingDays == 7) ? 0 : paddingDays;

        for (int i = 0; i < paddingDays; i++) {
            calendarDays.add(null);
        }
        for (int i = 1; i <= targetYearMonth.lengthOfMonth(); i++) {
            calendarDays.add(targetYearMonth.atDay(i));
        }
        
        // 曜日ラベル
        List<String> dayLabels = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) { // 月曜始まり
            dayLabels.add(day.getDisplayName(TextStyle.SHORT, Locale.JAPANESE));
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("currentDate", today);
        model.addAttribute("currentYearMonth", targetYearMonth);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("loggedDates", loggedDates);
        model.addAttribute("dayLabels", dayLabels);
        
        // 前月・翌月リンク用
        model.addAttribute("prevYear", targetYearMonth.minusMonths(1).getYear());
        model.addAttribute("prevMonth", targetYearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("nextYear", targetYearMonth.plusMonths(1).getYear());
        model.addAttribute("nextMonth", targetYearMonth.plusMonths(1).getMonthValue());

        return "log/meal-log"; // 新しいカレンダーテンプレート
    }

    /** * 食事記録フォームの表示 (モーダル用フラグメント)
     * GET /log/meal/form?date=2023-01-01
     */
    @GetMapping("/form")
    public String showMealLogForm(@RequestParam(value = "date", required = false) String date, Model model) {
        MealLogForm form = new MealLogForm();
        
        // 指定された日付があればセット、なければ今日
        if (date != null && !date.isEmpty()) {
            form.setDate(date);
        } else {
            form.setDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        }
        form.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        
        model.addAttribute("mealLogForm", form);
        return "log/meal-log-form"; // フラグメント化されたフォームテンプレート
    }

    /** 食事記録の保存 */
    @PostMapping("/save")
    public String saveMealLog(@Valid @ModelAttribute("mealLogForm") MealLogForm form, 
                              BindingResult result, 
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        
        if (result.hasErrors()) {
            // エラー時はフォームを再表示（通常のページ遷移として表示されてしまうため、
            // 実際はAJAXでエラーハンドリングするか、簡易的にカレンダーへ戻す等の対応が必要）
            // ここでは簡易的にフォーム単体を表示
            return "log/meal-log-form";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        MealRecord savedRecord = mealService.saveMealRecord(form, user);

        // 保存後はカレンダー画面（その記録の年月）にリダイレクト
        LocalDate date = savedRecord.getMealDateTime().toLocalDate();
        return "redirect:/log/meal?year=" + date.getYear() + "&month=" + date.getMonthValue();
    }
    
    /** 食事記録の一覧表示 (変更なし) */
    @GetMapping("/all")
    public String showAllMealLogs(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<MealRecord> mealRecords = mealService.getMealRecordsByUser(user);
        
        model.addAttribute("mealRecords", mealRecords);
        return "log/meal-log-all"; 
    }
}