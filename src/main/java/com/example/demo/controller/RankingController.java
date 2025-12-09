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
        // ★ 変更: Top20ではなく、全員取得するメソッドを呼び出し
        // List<User> rankingList = userRepository.findTop20ByOrderByLevelDescExperiencePointsDesc();
        List<User> rankingList = userRepository.findAllByOrderByLevelDescXpDesc();
        
        model.addAttribute("rankingList", rankingList);
        return "misc/ranking"; 
    }
}