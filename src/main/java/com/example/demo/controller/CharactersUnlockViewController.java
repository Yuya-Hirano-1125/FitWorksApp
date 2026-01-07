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
import org.springframework.web.bind.annotation.RequestParam; // ★追加

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
    public String showUnlockPage(
            Model model, 
            Principal principal,
            @RequestParam(value = "from", required = false) String from) { // ★追加: fromパラメータ
        
        // 1. ユーザー情報を取得
        String username = principal.getName();
        User user = userService.findByUsername(username);
        
        int userLevel = (user != null) ? user.getLevel() : 1;
        Set<Long> unlockedIds = (user != null) ? user.getUnlockedCharacters() : new HashSet<>();
        if (unlockedIds == null) unlockedIds = new HashSet<>();

        // 選択中のキャラクターID
        Long selectedCharacterId = (user != null) ? user.getSelectedCharacterId() : null;
        if (selectedCharacterId == null) selectedCharacterId = 0L; 
        model.addAttribute("selectedCharacterId", selectedCharacterId);

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
        Map<String, Long> nameToIdMap = allChars.stream()
            .collect(Collectors.toMap(CharacterEntity::getName, CharacterEntity::getId));

        for (CharacterEntity chara : allChars) {
            characterService.applyEvolutionData(chara);
        }

        // 解放可否状態の判定
        Map<Long, String> unlockStatusMap = new HashMap<>();
        for (CharacterEntity chara : allChars) {
            String status = "OK";
            Map<String, String> conditions = chara.getEvolutionConditions();
            if (conditions != null && conditions.containsKey("前提キャラ")) {
                String prereqName = conditions.get("前提キャラ");
                Long prereqId = nameToIdMap.get(prereqName);
                if (prereqId != null && !unlockedIds.contains(prereqId)) {
                    status = "PREREQ_LOCKED";
                }
            }
            if ("OK".equals(status) && userLevel < chara.getRequiredLevel()) {
                status = "LEVEL_LOCKED";
            }
            unlockStatusMap.put(chara.getId(), status);
        }

        // カテゴリーリスト作成
        List<Map<String, Object>> categoryList = new ArrayList<>();
        categoryList.add(createCategoryMap("fire", "炎属性", "fa-fire", filterByAttr(allChars, "fire")));
        categoryList.add(createCategoryMap("water", "水属性", "fa-droplet", filterByAttr(allChars, "water")));
        categoryList.add(createCategoryMap("grass", "草属性", "fa-leaf", filterByAttr(allChars, "grass")));
        categoryList.add(createCategoryMap("light", "光属性", "fa-sun", filterByAttr(allChars, "light")));
        categoryList.add(createCategoryMap("dark", "闇属性", "fa-moon", filterByAttr(allChars, "dark")));
        categoryList.add(createCategoryMap("secret", "シークレット", "fa-star", filterByAttr(allChars, "secret")));

        model.addAttribute("categoryList", categoryList);
        
        model.addAttribute("userLevel", userLevel);
        model.addAttribute("unlockedIds", unlockedIds);
        model.addAttribute("materialCounts", materialCounts);
        model.addAttribute("unlockStatusMap", unlockStatusMap);
        
        // ★追加: 遷移元情報を画面に渡す
        model.addAttribute("from", from);

        return "characters/menu/CharactersUnlock";
    }

    // ヘルパーメソッド: カテゴリーマップの作成
    private Map<String, Object> createCategoryMap(String key, String name, String icon, List<CharacterEntity> list) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", key);
        map.put("name", name);
        map.put("icon", icon);
        map.put("list", list);
        return map;
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