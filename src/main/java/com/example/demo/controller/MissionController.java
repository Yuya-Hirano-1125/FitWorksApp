package com.example.demo.controller;

import java.util.List; // ★ 修正: Listをインポート

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
     * デイリーミッション画面を表示する (今日のミッションがなければ自動生成)
     * GET /daily-mission に対応
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

        // ミッションステータスを取得してモデルに追加
        MissionStatusDto missionStatus = userService.getDailyMissionStatus(user);
        model.addAttribute("missionStatus", missionStatus);

        // MissionServiceから今日のミッションリストを取得 (日次リセット機能を含む)
        List<DailyMissionStatus> missions = missionService.getOrCreateTodayMissions(user);
        model.addAttribute("missions", missions);

        
        // 経験値に関する属性をモデルに追加
        model.addAttribute("level", user.getLevel());
        model.addAttribute("experiencePoints", user.getExperiencePoints());
        model.addAttribute("requiredXp", user.calculateRequiredXp());
        model.addAttribute("progressPercent", user.getProgressPercent());

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

        // MissionServiceに報酬獲得処理を委譲 (MissionServiceの claimMissionReward(Long, Long) を呼び出す)
        boolean success = missionService.claimMissionReward(user.getId(), missionId);
       
        if (success) {

            MissionStatusDto status = userService.getDailyMissionStatus(user);
            // 成功メッセージに獲得XPと現在のレベル・経験値を表示

 
            redirectAttributes.addFlashAttribute("successMessage", 

                "ミッション報酬 " + status.getRewardXp() + " XPを受け取りました！ " +
                "現在のレベル: " + user.getLevel() + 
                " (経験値: " + user.getExperiencePoints() + "/" + user.calculateRequiredXp() + ")");


        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "報酬を受け取れませんでした。ミッションを完了しているか、既に受け取り済みか確認してください。");
        }
        
        // 報酬受け取り後はコミュニティへ遷移
        return "redirect:/community";
    }
}
