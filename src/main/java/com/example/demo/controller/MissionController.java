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
        
        // ミッションステータスを取得してモデルに追加
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
    
    /**
     * 報酬受け取り処理 → みんなの広場へ遷移
     */
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
            MissionStatusDto status = userService.getDailyMissionStatus(user);
            // 成功メッセージに獲得XPと現在のレベル・経験値を表示
            redirectAttributes.addFlashAttribute("successMessage", 
                "ミッション報酬 " + status.getRewardXp() + " XPを受け取りました！ " +
                "現在のレベル: " + user.getLevel() + 
                " (経験値: " + user.getExperiencePoints() + "/" + user.calculateRequiredXp() + ")");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "報酬を受け取れませんでした。ミッションを完了しているか、または既に受け取り済みか確認してください。");
        }
        
        // 報酬受け取り後はコミュニティへ遷移
        return "redirect:/community";
    }
}
