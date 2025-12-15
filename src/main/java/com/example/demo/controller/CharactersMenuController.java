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

    // キャラクター一覧画面へ遷移
    @GetMapping("/characters/menu/CharactersStorage")
    public String showCharactersStorage() {
        return "characters/menu/CharactersStorage"; 
    }

    // キャラクター解放画面へ遷移
    @GetMapping("/characters/menu/CharactersUnlock")
    public String showCharactersUnlock() {
        return "characters/menu/CharactersUnlock"; 
    }

    /*
    // 進化素材画面へ遷移（素材一覧は MaterialController に統合済み）
    @GetMapping("/characters/menu/CharactersEvolutionMaterial")
    public String showCharactersEvolutionMaterial() {
        return "characters/menu/CharactersEvolutionMaterial"; 
    }
    */

    // --- 背景一覧画面へ遷移 ---
    @GetMapping("/characters/menu/Backgrounds")
    public String showBackgrounds() {
        return "characters/menu/Backgrounds"; 
    }
}