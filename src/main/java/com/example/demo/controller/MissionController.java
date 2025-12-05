package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.MissionStatusDto;
import com.example.demo.entity.DailyMissionStatus;
import com.example.demo.entity.User;
import com.example.demo.service.MissionService;
import com.example.demo.service.UserService; 

@Controller
@RequestMapping("/daily-mission")
public class MissionController {
    
    private final UserService userService;
    private final MissionService missionService;

    public MissionController(UserService userService, MissionService missionService) {
        this.userService = userService;
        this.missionService = missionService;
    }

    /**
     * デイリーミッション画面を表示
     */
    @GetMapping 
    public String showDailyMission(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return "redirect:/login";
        }

        MissionStatusDto missionStatus = userService.getDailyMissionStatus(user);
        model.addAttribute("missionStatus", missionStatus);

        List<DailyMissionStatus> missions = missionService.getOrCreateTodayMissions(user);
        model.addAttribute("missions", missions);

        model.addAttribute("level", user.getLevel());
        model.addAttribute("experiencePoints", user.getExperiencePoints());
        model.addAttribute("requiredXp", user.calculateRequiredXp());
        model.addAttribute("progressPercent", user.getProgressPercent());

        return "misc/daily-mission";
    }
    
    /**
     * FAQ画面
     */
    @GetMapping("/faq")
    public String showFaq() {
        return "misc/faq"; 
    }

    /**
     * AIコーチ画面に遷移
     */
    @GetMapping("/ai-coach")
    public String showAiCoachPage() {
        return "ai-coach/ai-coach-chat";
    }
    
    /**
     * ミッション報酬を獲得する処理
     * 成功したら AIコーチ画面へ遷移
     */
    @PostMapping("/claim/{missionId}")
    public String claimReward(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long missionId,
        RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return "redirect:/login";
        }

        boolean success = missionService.claimMissionReward(user.getId(), missionId);
       
        if (success) {
            MissionStatusDto status = userService.getDailyMissionStatus(user);
            redirectAttributes.addFlashAttribute("successMessage", 
                "ミッション報酬 " + status.getRewardXp() + " XPを受け取りました！ " +
                "現在のレベル: " + user.getLevel() + 
                " (経験値: " + user.getExperiencePoints() + "/" + user.calculateRequiredXp() + ")");
            // 成功時は AIコーチ画面へ
            return "redirect:/daily-mission/ai-coach";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "報酬を受け取れませんでした。ミッションを完了しているか、既に受け取り済みか確認してください。");
            return "redirect:/daily-mission";
        }
    }

    /**
     * ミッション1: ランニング → トレーニング画面へ遷移
     */
    @GetMapping("/running")
    public String showRunningMission() {
        // templates/training/training.html を返す
        return "training/training";
    }
}
