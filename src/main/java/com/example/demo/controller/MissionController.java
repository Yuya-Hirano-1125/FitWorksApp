package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MissionController {

    /**
     * デイリーミッション画面を表示する
     */
    @GetMapping("/daily-mission")
    public String showDailyMission() {
        // src/main/resources/templates/misc/daily-mission.html を返す
        return "misc/daily-mission";
    }

    /**
     * FAQ画面を表示する (★ このメソッドを追加・修正 ★)
     */
    @GetMapping("/faq")
    public String showFaq() {
        // テンプレートパスをフォルダ移動に合わせて修正
        return "misc/faq"; 
    }
} 