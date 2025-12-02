package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CharactersMenuController {

    // ホームのキャラクターボタンからキャラクターメニューへ遷移
    @GetMapping("/characters/menu/CharactersMenu")
    public String showCharactersMenu() {
        return "characters/menu/CharactersMenu"; 
    }
    @GetMapping("/characters/menu/CharactersStorage")
    public String showCharactersStorage() {
        return "/characters/menu/CharactersStorage"; 
    }
    @GetMapping("/characters/menu/CharactersUnlock")
    public String showCharactersUnlock() {
        return "/characters/menu/CharactersUnlock"; 
    }
}