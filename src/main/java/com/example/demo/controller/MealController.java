package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    /** 食事記録フォームの表示 */
    @GetMapping("/form")
    public String showMealLogForm(Model model) {
        MealLogForm form = new MealLogForm();
        // 初期値として現在の日付と時刻を設定
        form.setDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        form.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        
        model.addAttribute("mealLogForm", form);
        return "log/meal-log-form"; // Thymeleafテンプレート名
    }

    /** 食事記録の保存 */
    @PostMapping("/save")
    public String saveMealLog(@Valid @ModelAttribute("mealLogForm") MealLogForm form, 
                              BindingResult result, 
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("mealLogForm", form);
            return "log/meal-log-form";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        mealService.saveMealRecord(form, user);

        return "redirect:/log/meal/all"; 
    }
    
    /** 食事記録の一覧表示 */
    @GetMapping("/all")
    public String showAllMealLogs(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<MealRecord> mealRecords = mealService.getMealRecordsByUser(user);
        
        model.addAttribute("mealRecords", mealRecords);
        return "log/meal-log-all"; // 食事記録一覧のThymeleafテンプレート
    }
}