package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.model.CharacterUnlockStatus;
import com.example.demo.service.CharacterService;

@Controller
@RequestMapping("/characters")
public class CharacterStorageController {
    
    @Autowired
    private CharacterService characterService; 

    // ==========================================
    // キャラクター保管・一覧画面 (GET)
    // URL: http://localhost:8085/characters/storage
    // ==========================================
    @GetMapping("/storage")
    public String showCharacterStorage(Model model) {
        
        // 仮のユーザーID
        Long currentUserId = 1L; 
        int currentUserLevel = 50; 
        
        // Service層から、全キャラクターの解放状態を取得
        // ※CharacterService側でユーザーの所持状況を反映するロジックになっています
        List<CharacterUnlockStatus> charaList = characterService.getCharacterUnlockStatus(currentUserId); 
        
        // データをHTMLに渡す
        model.addAttribute("charaList", charaList); 
        model.addAttribute("currentLevel", currentUserLevel); 
        
        // テンプレートパス
        return "characters/menu/CharactersStorage"; 
    }
    
    // --- 以下、各キャラクター詳細画面への遷移メソッド ---
    @GetMapping("/dracoegg") public String DracoEggInfo() { return "characters/dracoegg"; }
    @GetMapping("/draco") public String DracoInfo() { return "characters/draco"; }
    @GetMapping("/dracos") public String DracosInfo() { return "characters/dracos"; }
    @GetMapping("/dragonoid") public String DragonoidInfo() { return "characters/dragonoid"; }
    
    @GetMapping("/dollyegg") public String DollyEggInfo() { return "characters/dollyegg"; }
    @GetMapping("/dolly") public String DollyInfo() { return "characters/dolly"; }
    @GetMapping("/dolphy") public String DolphyInfo() { return "characters/dolphy"; }
    @GetMapping("/dolphinas") public String DolphinasInfo() { return "characters/dolphinas"; }
    
    @GetMapping("/shiruegg") public String ShiruEggInfo() { return "characters/shiruegg"; }
    @GetMapping("/shiru") public String ShiruInfo() { return "characters/shiru"; }
    @GetMapping("/shirufa") public String ShirufaInfo() { return "characters/shirufa"; }
    @GetMapping("/shirufina") public String ShirufinaInfo() { return "characters/shirufina"; }
    
    @GetMapping("/merryegg") public String MerryEggInfo() { return "characters/merryegg"; }
    @GetMapping("/merry") public String MerryInfo() { return "characters/merry"; }
    @GetMapping("/meriru") public String MeriruInfo() { return "characters/Meriru"; }
    @GetMapping("/merinoa") public String MerinoaInfo() { return "characters/merinoa"; }
    
    @GetMapping("/robiegg") public String RobiEggInfo() { return "characters/robiegg"; }
    @GetMapping("/robi") public String RobiInfo() { return "characters/robi"; }
    @GetMapping("/robus") public String RobusInfo() { return "characters/robus"; }
    @GetMapping("/robius") public String RobiusInfo() { return "characters/robius"; }
}