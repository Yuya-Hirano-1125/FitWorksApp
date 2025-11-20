package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingController {

    /**
     * FAQページを表示する
     * URL: /faq にアクセスがあった場合に 'faq' (faq.html) テンプレートを返す
     */
    @GetMapping("/faq")
    public String showFaqPage() {
        // src/main/resources/templates/faq.html を指定
        return "faq"; 
    }
}