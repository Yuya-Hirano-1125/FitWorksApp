package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Controller
@RequestMapping("/ranking")
public class RankingController {

    private final UserRepository userRepository;

    public RankingController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(Model model) {
        // 強い順にユーザーリストを取得
        List<User> rankingList = userRepository.findTop20ByOrderByLevelDescExperiencePointsDesc();
        
        model.addAttribute("rankingList", rankingList);
        return "ranking"; // templates/ranking.html を表示
    }
}