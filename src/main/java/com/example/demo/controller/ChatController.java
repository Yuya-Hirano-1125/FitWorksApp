package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {
    
    // AuthControllerと競合しないよう、GETリクエストはここで処理
    @GetMapping("/ai-coach")
    public String aiCoachPage() {
        // モデルにデータを渡す必要はありません。JSがREST API経由でデータを取得・表示します。
        return "ai-coach-chat"; 
    }
}
