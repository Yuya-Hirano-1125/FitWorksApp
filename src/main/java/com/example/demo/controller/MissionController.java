package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.DailyMissionStatus;
import com.example.demo.entity.User;
import com.example.demo.service.MissionService;

@Controller
@RequestMapping("/mission")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    // デイリーミッション画面の表示
    @GetMapping("/daily")
    public String showDailyMissions(@AuthenticationPrincipal User user, Model model) {
        // 現在ログインしているUserエンティティを取得 (SecurityConfigでPrincipalがUser型に設定されている前提)
        // もしPrincipalがUserDetails型であれば、UserService.findByUsernameなどでUserを取得してください
        
        if (user == null) {
            return "redirect:/login"; // ログインしていない場合はリダイレクト
        }

        // ミッション進捗を「一から始める」処理を含む、今日のミッションリストを取得
        List<DailyMissionStatus> missions = missionService.getTodayMissions(user);
        
        model.addAttribute("missions", missions);
        model.addAttribute("user", user); // ユーザー情報も表示に利用可能
        
        return "misc/daily-mission"; // src/main/resources/templates/misc/daily-mission.html をレンダリング
    }

    // (例) 運動記録を保存するControllerからの連携
    // 運動記録Controllerなどからこのメソッドを呼び出すことで、ミッション進捗が更新されます。
    // 例として、MissionControllerにダミーの進捗更新エンドポイントを追加します。
    @PostMapping("/progress")
    public String updateMissionProgress(@AuthenticationPrincipal User user, @RequestParam String missionType) {
        if (user == null) {
            return "redirect:/login";
        }

        // MissionServiceに進捗更新を依頼
        missionService.updateMissionProgressAndCheckCompletion(user.getId(), missionType);

        return "redirect:/mission/daily";
    }
}



