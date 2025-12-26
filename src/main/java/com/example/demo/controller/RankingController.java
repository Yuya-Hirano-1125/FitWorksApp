package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.User;
import com.example.demo.service.MissionService; // ★追加
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/ranking")
public class RankingController {

    private final UserService userService;
    private final MissionService missionService; // ★追加

    public RankingController(UserService userService, MissionService missionService) { // ★追加
        this.userService = userService;
        this.missionService = missionService; // ★追加
    }

    @GetMapping
    public String index(Model model, Principal principal) {
        String username = principal.getName();
        
        // ★★★ ランキング確認ミッション進捗更新 ★★★
        // PrincipalからUserオブジェクトを取得してIDを使用
        User user = userService.findByUsername(username);
        if (user != null) {
            missionService.updateMissionProgress(user.getId(), "CHECK_RANKING");
        }

        // フレンド内ランキングを取得
        List<User> rankingList = userService.getFriendRanking(username);
        
        model.addAttribute("rankingList", rankingList);
        return "misc/ranking"; 
    }
}