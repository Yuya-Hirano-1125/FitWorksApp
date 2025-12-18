package com.example.demo.controller;

import java.security.Principal;
import java.util.ArrayList;
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

    @GetMapping("/characters")
    public String showCharacters(Model model, Principal principal) {
        // 全キャラクター一覧を取得
        List<CharacterEntity> characters = repository.findAll();

        // 進化素材・進化条件をセット
        characters.forEach(chara -> characterService.applyEvolutionData(chara));

        // デバッグ用ログ出力
        System.out.println("=== Character List ===");
        if (characters.isEmpty()) {
            System.out.println("キャラクターが1件も取得できませんでした。DBを確認してください。");
        } else {
            characters.forEach(c -> System.out.println(
                "ID=" + c.getId() +
                ", Name=" + c.getName() +
                ", Attribute=" + c.getAttribute() +
                ", Rarity=" + c.getRarity() +
                ", RequiredLevel=" + c.getRequiredLevel() +
                ", UnlockCost=" + c.getUnlockCost() +
                ", ImagePath=" + c.getImagePath() +
                ", EvolutionMaterials=" + c.getEvolutionMaterials() +
                ", EvolutionConditions=" + c.getEvolutionConditions()
            ));
        }

        // 属性ごとにフィルタリングして Model に渡す
        model.addAttribute("fireChars", characters.stream()
                .filter(c -> "fire".equals(c.getAttribute()))
                .toList());

        model.addAttribute("waterChars", characters.stream()
                .filter(c -> "water".equals(c.getAttribute()))
                .toList());

        model.addAttribute("grassChars", characters.stream()
                .filter(c -> "grass".equals(c.getAttribute()))
                .toList());

        model.addAttribute("lightChars", characters.stream()
                .filter(c -> "light".equals(c.getAttribute()))
                .toList());

        // 闇属性は4体まで表示(シークレット除外)
        model.addAttribute("darkChars", characters.stream()
                .filter(c -> "dark".equals(c.getAttribute()))
                .filter(c -> !"シークレット".equals(c.getName()))
                .limit(4)
                .toList());

        // シークレット属性は "secret" または名前がシークレットのものを表示
        model.addAttribute("secretChars", characters.stream()
                .filter(c -> "secret".equals(c.getAttribute()) || "シークレット".equals(c.getName()))
                .toList());

        // --- ★★★ 修正: UserServiceの専用メソッドで安全に取得 ★★★ ---
        int userLevel = 1;
        List<Long> unlockedIds = new ArrayList<>();
        
        if (principal != null) {
            String username = principal.getName();
            userLevel = userService.getUserLevel(username);
            
            // ★★★ @Transactional付きメソッドで安全に取得 ★★★
            unlockedIds = userService.getUnlockedCharacterIds(username);
            System.out.println("DEBUG: User=" + username + ", Level=" + userLevel + ", UnlockedCharacters=" + unlockedIds);
        }
        
        model.addAttribute("userLevel", userLevel);
        model.addAttribute("unlockedIds", unlockedIds);

        // 素材は仮のまま(後でDB連動に変更可能)
        model.addAttribute("userMaterials", Map.of(
                "fire", 20,
                "water", 15,
                "grass", 10,
                "light", 5,
                "dark", 8,
                "secret", 1
        ));

        // CharactersUnlock.html を返す
        return "characters/menu/CharactersUnlock";
    }
}