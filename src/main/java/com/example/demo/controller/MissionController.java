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
     */
    @GetMapping
    public String showDailyMission(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return "redirect:/login";
        }

        // 今日のミッションを取得
        List<DailyMissionStatus> missions = missionService.getOrCreateTodayMissions(user);
        model.addAttribute("missions", missions);

        // 経験値関連をモデルに追加
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
     * ミッション報酬受け取り処理
     */
    @PostMapping("/claim/{missionId}")
    public String claimReward(
            @AuthenticationPrincipal UserDetails userDetails,
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
            redirectAttributes.addFlashAttribute("successMessage",
                    "ミッション報酬を獲得し、経験値を得ました！");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "報酬を受け取れませんでした。ミッションを完了しているか、既に受け取り済みか確認してください。");
        }

        // ホーム画面にリダイレクト → XPバーに反映される
        return "redirect:/home";
    }
}
