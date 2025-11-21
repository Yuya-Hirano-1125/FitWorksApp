package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.MissionStatusDto;
import com.example.demo.entity.User;
import com.example.demo.service.UserService; 

@Controller
public class MissionController {
    
    private final UserService userService;
    
    public MissionController(UserService userService) {
        this.userService = userService;
    }

    /**
     * デイリーミッション画面を表示する
     */
    @GetMapping("/daily-mission")
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
        
        // ★ 修正: ミッションステータスを取得してモデルに追加
        MissionStatusDto missionStatus = userService.getDailyMissionStatus(user);
        model.addAttribute("missionStatus", missionStatus);
        
        // 経験値に関する属性もモデルに追加
        model.addAttribute("level", user.getLevel());
        model.addAttribute("experiencePoints", user.getExperiencePoints());
        model.addAttribute("requiredXp", user.calculateRequiredXp());
        model.addAttribute("progressPercent", user.getProgressPercent());

        // src/main/resources/templates/misc/daily-mission.html を返す
        return "misc/daily-mission";
    }
    
    /**
     * FAQ画面を表示する
     */
    @GetMapping("/faq")
    public String showFaq() {
        return "misc/faq"; 
    }
    
    // ★ 新規追加: 報酬受け取り処理
    @PostMapping("/daily-mission/claim")
    public String claimReward(
        @AuthenticationPrincipal UserDetails userDetails,
        RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return "redirect:/login";
        }

        boolean success = userService.claimMissionReward(user);
        
        if (success) {
            // 成功メッセージに獲得XPを表示
            redirectAttributes.addFlashAttribute("successMessage", 
                "ミッション報酬 " + userService.getDailyMissionStatus(user).getRewardXp() + " XPを受け取りました！");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "報酬を受け取れませんでした。ミッションを完了しているか、または既に受け取り済みか確認してください。");
        }
        
        return "redirect:/daily-mission";
    }
}