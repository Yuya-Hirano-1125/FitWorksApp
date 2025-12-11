package com.example.demo.controller;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.CharacterService;

@Controller
@RequestMapping("/characters")
public class CharacterUnlockController {

    @Autowired
    private CharacterService characterService;

    // ==========================================
    // 進化画面を表示する機能 (GET)
    // ==========================================
    @GetMapping("/unlock")
    public String showUnlockPage(Model model) {
        
        Long currentUserId = 1L; 

        // サービスからID一覧を取得
        Set<Long> unlockedIds = characterService.getUnlockedCharacterIds(currentUserId);
        
        // ★修正点: もしnullだったら空のセットにしてエラー回避
        if (unlockedIds == null) {
            unlockedIds = Collections.emptySet();
            System.out.println("WARN: unlockedIds was null. Initialized to empty set.");
        }
        
        // ログで確認
        System.out.println("DEBUG: unlockedIds = " + unlockedIds);

        // 画面に渡す
        model.addAttribute("unlockedIds", unlockedIds);

        return "characters/menu/CharactersUnlock"; 
    }

    // ==========================================
    // 進化処理を実行する機能 (POST)
    // ==========================================
    @PostMapping("/unlock")
    public String unlock(
            @RequestParam("characterId") Integer characterId,
            @RequestParam("cost") Integer cost,
            RedirectAttributes ra
    ) {
        Long currentUserId = 1L; 

        try {
            characterService.unlockCharacter(currentUserId, Long.valueOf(characterId), cost);
            ra.addFlashAttribute("message", "キャラクターを進化させました！");
            
            // 成功したらStorage画面へ
            return "redirect:/characters/storage";
            
        } catch (Exception e) {
            e.printStackTrace(); // コンソールにエラー詳細を出す
            ra.addFlashAttribute("error", "進化失敗: " + e.getMessage());
            return "redirect:/characters/unlock"; 
        }
    }
}