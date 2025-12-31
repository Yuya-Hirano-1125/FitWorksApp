package com.example.demo.controller;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/friends")
public class FriendController {

    private final UserService userService;

    public FriendController(UserService userService) {
        this.userService = userService;
    }

    // フレンド画面トップ (一覧・申請待ち表示)
    @GetMapping
    public String index(Model model, Principal principal) {
        String username = principal.getName();
        User currentUser = userService.findByUsername(username);

        // フレンド一覧
        Set<User> friends = currentUser.getFriends();
        // 承認待ちリクエスト
        Set<User> requests = currentUser.getReceivedFriendRequests();

        // 既にフレンドになっているユーザーからの申請があればDBから削除する（念のためのクリーンアップ）
        List<User> invalidRequests = requests.stream()
            .filter(req -> friends.stream().anyMatch(f -> f.getId().equals(req.getId())))
            .collect(Collectors.toList());

        if (!invalidRequests.isEmpty()) {
            for (User u : invalidRequests) {
                currentUser.removeReceivedFriendRequest(u);
            }
            userService.save(currentUser); 
            requests = currentUser.getReceivedFriendRequests(); // 最新化
        }

        model.addAttribute("friends", friends);
        model.addAttribute("requests", requests);
        model.addAttribute("currentSection", "list"); 

        return "friends/index"; 
    }

    // ユーザー検索
    @GetMapping("/search")
    public String search(@RequestParam(name = "keyword", required = false) String keyword,
                         Model model, Principal principal) {
        String username = principal.getName();
        User currentUser = userService.findByUsername(username);

        List<User> searchResults = userService.searchUsers(username, keyword);
        
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchResults", searchResults);
        
        // 既存情報も渡す
        model.addAttribute("friends", currentUser.getFriends());
        model.addAttribute("requests", currentUser.getReceivedFriendRequests());
        model.addAttribute("currentSection", "search");

        return "friends/index";
    }

    // フレンド申請送信
    @PostMapping("/request/{userId}")
    public String sendRequest(@PathVariable("userId") Long userId, 
                              Principal principal, RedirectAttributes redirectAttributes) {
        try {
            // ★修正: 戻り値(int)で分岐
            // 0: 失敗, 1: 申請送信, 2: フレンド成立
            int status = userService.sendFriendRequest(principal.getName(), userId);
            
            if (status == 1) {
                redirectAttributes.addFlashAttribute("successMessage", "フレンド申請を送りました！");
            } else if (status == 2) {
                redirectAttributes.addFlashAttribute("successMessage", "フレンドになりました！");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "既にフレンドか、申請済みです。");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました。");
        }
        return "redirect:/friends/search?keyword="; 
    }

    // 承認
    @PostMapping("/approve/{userId}")
    public String approveRequest(@PathVariable("userId") Long userId, Principal principal) {
        userService.approveFriendRequest(principal.getName(), userId);
        return "redirect:/friends";
    }

    // 拒否
    @PostMapping("/reject/{userId}")
    public String rejectRequest(@PathVariable("userId") Long userId, Principal principal) {
        userService.rejectFriendRequest(principal.getName(), userId);
        return "redirect:/friends";
    }

    // フレンド解除
    @PostMapping("/remove/{userId}")
    public String removeFriend(@PathVariable("userId") Long userId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            userService.removeFriend(principal.getName(), userId);
            redirectAttributes.addFlashAttribute("infoMessage", "フレンドを解除しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "解除に失敗しました。");
        }
        return "redirect:/friends";
    }
}