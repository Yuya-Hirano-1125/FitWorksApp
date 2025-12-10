package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/ranking")
public class RankingController {

    private final UserService userService;

    public RankingController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String index(Model model, Principal principal) {
        String username = principal.getName();
        
        // ★変更: 全員ではなく、フレンド内ランキングを取得
        // List<User> rankingList = userRepository.findAllByOrderByLevelDescXpDesc();
        List<User> rankingList = userService.getFriendRanking(username);
        
        model.addAttribute("rankingList", rankingList);
        return "misc/ranking"; 
    }

    // ★追加: フレンド追加処理
    @PostMapping("/add")
    public String addFriend(@RequestParam("targetUsername") String targetUsername, 
                            Principal principal, 
                            RedirectAttributes redirectAttributes) {
        String currentUsername = principal.getName();
        
        try {
            boolean success = userService.addFriendByUsername(currentUsername, targetUsername);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", targetUsername + "さんをフレンドに追加しました！");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが見つからないか、既にフレンド、または自分自身です。");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました: " + e.getMessage());
        }
        
        return "redirect:/ranking";
    }
}