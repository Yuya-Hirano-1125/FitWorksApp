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
        // src/main/resources/templates/daily-mission.html を返す
        return "daily-mission";
    }

}