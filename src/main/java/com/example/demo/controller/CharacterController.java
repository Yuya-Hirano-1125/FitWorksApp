package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/characters")
public class CharacterController {
    @GetMapping("/storage") // URL: /characters/storage
    public String showCharacterStorage() {
        return "characters/Storage"; // テンプレート名: src/main/resources/templates/characters/Storage.html
    }
}