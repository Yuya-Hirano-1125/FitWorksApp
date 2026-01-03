package com.example.demo.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.CharacterEntity;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.service.CharacterService;
import com.example.demo.service.UserService;

@Controller
public class CharacterViewController {

    private final CharacterRepository repository;
    private final UserService userService;
    private final CharacterService characterService;

    public CharacterViewController(CharacterRepository repository,
                                   UserService userService,
                                   CharacterService characterService) {
        this.repository = repository;
        this.userService = userService;
        this.characterService = characterService;
    }

    /**
     * キャラクター一覧画面
     */
    @GetMapping("/characters")
    public String showCharacters(Model model, Principal principal) {
        // 全キャラクター一覧を取得
        List<CharacterEntity> characters = repository.findAll();

        // 進化素材・進化条件をセット
        characters.forEach(chara -> characterService.applyEvolutionData(chara));

        // ユーザー情報を取得
        String username = principal.getName();
        int userLevel = 1;
        var unlockedIds = new java.util.HashSet<Long>();
        unlockedIds.add(0L); // 初期キャラ

        if (username != null) {
            userLevel = userService.getUserLevel(username);
            var dbUnlocked = userService.getUnlockedCharacterIds(username);
            if (dbUnlocked != null) {
                unlockedIds.addAll(dbUnlocked);
            }
        }
        
        model.addAttribute("userLevel", userLevel);
        model.addAttribute("unlockedIds", unlockedIds);

        // 仮の素材データ（一覧画面用）
        model.addAttribute("userMaterials", Map.of(
                "fire", 20, "water", 15, "grass", 10, "light", 5, "dark", 8, "secret", 1
        ));
        
        model.addAttribute("characters", characters);

        return "characters/menu/CharactersMenu";
    }

    // ★★★ 注意: ここにあった showUnlockPage メソッドは削除します ★★★
    // CharactersUnlockViewController がこの役割を担当するためです。
}