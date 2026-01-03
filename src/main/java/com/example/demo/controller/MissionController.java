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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.MissionStatusDto;
import com.example.demo.entity.DailyMissionStatus;
import com.example.demo.entity.User;
import com.example.demo.service.CommunityService;
import com.example.demo.service.MissionService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/daily-mission")
public class MissionController {

    private final UserService userService;
    private final MissionService missionService;
    private final CommunityService communityService;

    public MissionController(UserService userService,
                             MissionService missionService,
                             CommunityService communityService) {
        this.userService = userService;
        this.missionService = missionService;
        this.communityService = communityService;
    }

    /**
     * デイリーミッション画面を表示
     */
    @GetMapping
    public String showDailyMission(@AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return "redirect:/login";
        }

        // ミッション進捗情報
        MissionStatusDto missionStatus = userService.getDailyMissionStatus(user);
        model.addAttribute("missionStatus", missionStatus);

        // 今日のミッション一覧
        List<DailyMissionStatus> missions = missionService.getOrCreateTodayMissions(user);
        model.addAttribute("missions", missions);

        // ユーザー情報
        model.addAttribute("level", user.getLevel());
        model.addAttribute("experiencePoints", user.getExperiencePoints());
        model.addAttribute("requiredXp", user.calculateRequiredXp());
        model.addAttribute("progressPercent", user.getProgressPercent());

        return "misc/daily-mission";
    }

    /**
     * AIコーチ画面に遷移
     */
    @GetMapping("/ai-coach")
    public String showAiCoachPage(@AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                missionService.updateMissionProgress(user.getId(), "AI_COACH");
            }
        }
        return "ai-coach/ai-coach-chat";
    }

    /**
     * ミッション報酬を獲得する処理
     */
    @PostMapping("/claim/{missionId}")
    public String claimReward(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long missionId,
                              RedirectAttributes redirectAttributes) {
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
            User updatedUser = userService.findByUsername(userDetails.getUsername()); // 最新情報を取得

            // ★修正: メッセージを「コイン」に変更
            redirectAttributes.addFlashAttribute("successMessage",
                    "ミッション報酬 " + status.getRewardXp() + " XPと " + 
                    MissionService.DAILY_MISSION_REWARD_CHIPS + " コインを受け取りました！");
            
            // ★追加: 画面表示用に個別にデータを渡す
            redirectAttributes.addFlashAttribute("rewardLevel", updatedUser.getLevel());
            // ★修正: 変数名を rewardCoins に変更
            redirectAttributes.addFlashAttribute("rewardCoins", updatedUser.getChipCount());
            redirectAttributes.addFlashAttribute("rewardExp", updatedUser.getExperiencePoints());
            redirectAttributes.addFlashAttribute("rewardReqExp", updatedUser.calculateRequiredXp());
            
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "報酬を受け取れませんでした。ミッションを完了しているか、既に受け取り済みか確認してください。");
        }
        return "redirect:/daily-mission";
    }
    
    /**
     * ミッション1: ランニング → トレーニング画面へ遷移
     */
    @GetMapping("/running")
    public String showRunningMission(@AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                missionService.updateMissionProgress(user.getId(), "TRAINING_LOG");
                redirectAttributes.addFlashAttribute("successMessage", "ランニングを記録しました！ミッション進捗が更新されました。");
            }
        }
        return "training/training";
    }

    @PostMapping("/community/post")
    public String postCommunity(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String content,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return "redirect:/login";
        }

        communityService.createPost(user, content, content);
        missionService.updateMissionProgress(user.getId(), "COMMUNITY_POST");

        redirectAttributes.addFlashAttribute("successMessage", "コミュニティに投稿しました！ミッション進捗が更新されました。");
        return "redirect:/daily-mission";
    }
}