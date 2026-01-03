package com.example.demo.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.CharacterEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.service.CharacterService;
import com.example.demo.service.UserService;

@Controller
public class CharactersUnlockViewController {

    private final CharacterRepository characterRepository;
    private final UserService userService;
    private final CharacterService characterService;

    public CharactersUnlockViewController(CharacterRepository characterRepository, 
                                          UserService userService, 
                                          CharacterService characterService) {
        this.characterRepository = characterRepository;
        this.userService = userService;
        this.characterService = characterService;
    }

    @GetMapping("/characters/menu/CharactersUnlock")
    public String showUnlockPage(Model model, Principal principal) {
        // 1. ユーザー情報を取得
        String username = principal.getName();
        User user = userService.findByUsername(username);
        
        int userLevel = (user != null) ? user.getLevel() : 1;
        Set<Long> unlockedIds = (user != null) ? user.getUnlockedCharacters() : new HashSet<>();
        if (unlockedIds == null) unlockedIds = new HashSet<>();

        // 2. 素材所持数の取得
        Map<String, Integer> materialCounts = new HashMap<>();
        for (int i = 1; i <= 16; i++) {
            Long itemId = (long) i;
            try {
                int count = userService.getUserMaterialCount(username, itemId);
                materialCounts.put(String.valueOf(i), count);
            } catch (Exception e) {
                materialCounts.put(String.valueOf(i), 0);
            }
        }

        // 3. 全キャラクターを取得し、進化条件をセット
        List<CharacterEntity> allChars = characterRepository.findAll();
        // 名前からIDを引けるマップを作成 (前提キャラ判定用)
        Map<String, Long> nameToIdMap = allChars.stream()
            .collect(Collectors.toMap(CharacterEntity::getName, CharacterEntity::getId));

        for (CharacterEntity chara : allChars) {
            characterService.applyEvolutionData(chara);
        }

        // ★追加: 各キャラの解放可否状態("OK", "LEVEL_LOCKED", "PREREQ_LOCKED")を判定
        Map<Long, String> unlockStatusMap = new HashMap<>();
        
        for (CharacterEntity chara : allChars) {
            String status = "OK";
            
            // 判定1: 前提キャラチェック
            Map<String, String> conditions = chara.getEvolutionConditions();
            if (conditions != null && conditions.containsKey("前提キャラ")) {
                String prereqName = conditions.get("前提キャラ");
                Long prereqId = nameToIdMap.get(prereqName);
                if (prereqId != null && !unlockedIds.contains(prereqId)) {
                    status = "PREREQ_LOCKED"; // 前提キャラ未所持
                }
            }
            
            // 判定2: レベルチェック (前提チェックでNGなら上書きしない)
            if ("OK".equals(status) && userLevel < chara.getRequiredLevel()) {
                status = "LEVEL_LOCKED";
            }
            
            unlockStatusMap.put(chara.getId(), status);
        }

        // 4. モデルへの追加
        model.addAttribute("fireChars", filterByAttr(allChars, "fire"));
        model.addAttribute("waterChars", filterByAttr(allChars, "water"));
        model.addAttribute("grassChars", filterByAttr(allChars, "grass"));
        model.addAttribute("lightChars", filterByAttr(allChars, "light"));
        model.addAttribute("darkChars", filterByAttr(allChars, "dark"));
        model.addAttribute("secretChars", filterByAttr(allChars, "secret"));

        model.addAttribute("userLevel", userLevel);
        model.addAttribute("unlockedIds", unlockedIds);
        model.addAttribute("materialCounts", materialCounts);
        model.addAttribute("unlockStatusMap", unlockStatusMap); // ★追加

        return "characters/menu/CharactersUnlock";
    }

    private List<CharacterEntity> filterByAttr(List<CharacterEntity> list, String attr) {
        List<CharacterEntity> filtered = new ArrayList<>();
        if (list == null) return filtered;
        for (CharacterEntity c : list) {
            if (c.getAttribute() != null && c.getAttribute().equalsIgnoreCase(attr)) {
                filtered.add(c);
            }
        }
        filtered.sort((c1, c2) -> Integer.compare(c1.getRequiredLevel(), c2.getRequiredLevel()));
        return filtered;
    }
}