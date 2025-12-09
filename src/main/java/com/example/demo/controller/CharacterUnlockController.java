package com.example.demo.controller;

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
    // URL: http://localhost:8085/characters/unlock
    // ==========================================
    @GetMapping("/unlock")
    public String showUnlockPage(Model model) {
        
        // 仮のユーザーID
        Long currentUserId = 1L; 

        // ★ここが重要: ユーザーが既に持っているキャラのID一覧を取得してHTMLに渡す
        // これがないとHTML側で「unlockedIdsが見つからない」というエラーになり、途中までしか表示されません
        Set<Long> unlockedIds = characterService.getUnlockedCharacterIds(currentUserId);
        model.addAttribute("unlockedIds", unlockedIds);

        // テンプレートのパス (characters/menu/フォルダにある場合)
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
        } catch (Exception e) {
            ra.addFlashAttribute("error", "失敗しました: " + e.getMessage());
        }

        return "redirect:/characters/unlock"; 
    }
}