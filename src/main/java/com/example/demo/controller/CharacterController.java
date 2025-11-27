package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/characters")
public class CharacterController {

    @GetMapping("/storage")
    public String showCharacterStorage() {
        return "characters/Storage"; 
    }
    
    @GetMapping("/draco") 
    public String dracoInfo() {
        // 【修正】フォルダ階層を含めて指定する必要があります
        // templates/characters/draco.html を読み込む場合
        return "characters/draco"; 
    }
}