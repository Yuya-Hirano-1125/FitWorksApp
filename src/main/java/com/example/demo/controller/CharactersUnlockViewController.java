package com.example.demo.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.CharacterEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.service.CharacterService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/characters")  // ★ ここを追加
public class CharactersUnlockViewController {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private UserService userService;  // ★ここで注入されている

    @Autowired
    private CharacterService characterService;
    
    /**
     * 素材名からitem_idを取得するヘルパーメソッド
     */
    private Long getMaterialItemId(String materialType) {
        switch (materialType) {
            case "紅玉": return 1L;
            case "蒼玉": return 2L;
            case "翠玉": return 3L;
            case "聖玉": return 4L;
            case "闇玉": return 5L;
            case "赤の聖結晶": return 6L;
            case "青の聖結晶": return 7L;
            case "緑の聖結晶": return 8L;
            case "黄の聖結晶": return 9L;
            case "紫の聖結晶": return 10L;
            case "赫焔鱗": return 11L;
            case "氷華の杖": return 12L;     // ★修正
            case "緑晶燈": return 13L;
            case "夢紡ぎの枕": return 14L;
            case "月詠みの杖": return 15L;   // ★修正
            default: 
                System.err.println("WARNING: Unknown material type: " + materialType);
                return 1L;
        }
    }

    @GetMapping("/characters/unlock")
    public String showUnlockPage(Model model, Principal principal) {
        // ★★★ 最初に空のマップで初期化 ★★★
        Map<String, Integer> materialCounts = new HashMap<>();
        materialCounts.put("紅玉", 0);
        materialCounts.put("蒼玉", 0);
        materialCounts.put("翠玉", 0);
        materialCounts.put("聖玉", 0);
        materialCounts.put("闇玉", 0);
        materialCounts.put("赤の聖結晶", 0);
        materialCounts.put("青の聖結晶", 0);
        materialCounts.put("緑の聖結晶", 0);
        materialCounts.put("黄の聖結晶", 0);
        materialCounts.put("紫の聖結晶", 0);
        materialCounts.put("赫焔鱗", 0);
        materialCounts.put("氷華の杖", 0);
        materialCounts.put("緑晶燈", 0);
        materialCounts.put("夢紡ぎの枕", 0);
        materialCounts.put("月詠みの杖", 0);
     // ★★★ username を try の外で宣言 ★★★
        String username = principal.getName();
        try {
            System.out.println("DEBUG: Starting material count retrieval...");
            
            int redCount = userService.getUserMaterialCount(username, getMaterialItemId("紅玉"));
            System.out.println("DEBUG: 紅玉 item_id=1, count=" + redCount);
            materialCounts.put("紅玉", redCount);
            
            int blueCount = userService.getUserMaterialCount(username, getMaterialItemId("蒼玉"));
            System.out.println("DEBUG: 蒼玉 item_id=2, count=" + blueCount);
            materialCounts.put("蒼玉", blueCount);
            
            int greenCount = userService.getUserMaterialCount(username, getMaterialItemId("翠玉"));
            System.out.println("DEBUG: 翠玉 item_id=3, count=" + greenCount);
            materialCounts.put("翠玉", greenCount);
            
            // ... 他の素材も同様
            
            System.out.println("DEBUG: 素材所持数取得成功 - " + materialCounts);
        } catch (Exception e) {
            System.err.println("ERROR: 素材所持数の取得に失敗しました - " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            User user = userService.findByUsername(username);
            
            if (user == null) {
                System.err.println("ERROR: User not found for username: " + username);
                model.addAttribute("materialCounts", materialCounts);
                model.addAttribute("userLevel", 1);
                model.addAttribute("unlockedIds", new HashSet<>());
                model.addAttribute("fireChars", new ArrayList<>());
                model.addAttribute("waterChars", new ArrayList<>());
                model.addAttribute("grassChars", new ArrayList<>());
                model.addAttribute("lightChars", new ArrayList<>());
                model.addAttribute("darkChars", new ArrayList<>());
                model.addAttribute("secretChars", new ArrayList<>());
                return "characters/CharactersUnlock";
            }

            System.out.println("DEBUG: User found: " + username + ", Level: " + user.getLevel());

            // 各属性のキャラクターを取得
            List<CharacterEntity> fireChars = characterRepository.findByAttribute("fire");
            List<CharacterEntity> waterChars = characterRepository.findByAttribute("water");
            List<CharacterEntity> grassChars = characterRepository.findByAttribute("grass");
            List<CharacterEntity> lightChars = characterRepository.findByAttribute("light");
            List<CharacterEntity> darkChars = characterRepository.findByAttribute("dark");
            List<CharacterEntity> secretChars = characterRepository.findByAttribute("secret");

            System.out.println("DEBUG: Characters loaded - Fire: " + fireChars.size() + ", Water: " + waterChars.size());

            // 各キャラクターに進化データを適用
            for (CharacterEntity chara : fireChars) {
                characterService.applyEvolutionData(chara);
            }
            for (CharacterEntity chara : waterChars) {
                characterService.applyEvolutionData(chara);
            }
            for (CharacterEntity chara : grassChars) {
                characterService.applyEvolutionData(chara);
            }
            for (CharacterEntity chara : lightChars) {
                characterService.applyEvolutionData(chara);
            }
            for (CharacterEntity chara : darkChars) {
                characterService.applyEvolutionData(chara);
            }
            for (CharacterEntity chara : secretChars) {
                characterService.applyEvolutionData(chara);
            }

            // 解放済みキャラクターIDを取得
            Set<Long> unlockedIds = user.getUnlockedCharacters();
            if (unlockedIds == null) {
                unlockedIds = new HashSet<>();
            }
            
            System.out.println("DEBUG: Unlocked characters: " + unlockedIds.size());

            // ★ 素材所持数を取得（実際の値で上書き）
            try {
                System.out.println("DEBUG: Starting material count retrieval...");
                
                materialCounts.put("紅玉", userService.getUserMaterialCount(username, getMaterialItemId("紅玉")));
                materialCounts.put("蒼玉", userService.getUserMaterialCount(username, getMaterialItemId("蒼玉")));
                materialCounts.put("翠玉", userService.getUserMaterialCount(username, getMaterialItemId("翠玉")));
                materialCounts.put("聖玉", userService.getUserMaterialCount(username, getMaterialItemId("聖玉")));
                materialCounts.put("闇玉", userService.getUserMaterialCount(username, getMaterialItemId("闇玉")));
                materialCounts.put("赤の聖結晶", userService.getUserMaterialCount(username, getMaterialItemId("赤の聖結晶")));
                materialCounts.put("青の聖結晶", userService.getUserMaterialCount(username, getMaterialItemId("青の聖結晶")));
                materialCounts.put("緑の聖結晶", userService.getUserMaterialCount(username, getMaterialItemId("緑の聖結晶")));
                materialCounts.put("黄の聖結晶", userService.getUserMaterialCount(username, getMaterialItemId("黄の聖結晶")));
                materialCounts.put("紫の聖結晶", userService.getUserMaterialCount(username, getMaterialItemId("紫の聖結晶")));
                materialCounts.put("赫焔鱗", userService.getUserMaterialCount(username, getMaterialItemId("赫焔鱗")));
                materialCounts.put("氷華の杖", userService.getUserMaterialCount(username, getMaterialItemId("氷華の杖")));
                materialCounts.put("緑晶燈", userService.getUserMaterialCount(username, getMaterialItemId("緑晶燈")));
                materialCounts.put("夢紡ぎの枕", userService.getUserMaterialCount(username, getMaterialItemId("夢紡ぎの枕")));
                materialCounts.put("月詠みの杖", userService.getUserMaterialCount(username, getMaterialItemId("月詠みの杖")));
                
                System.out.println("DEBUG: 素材所持数取得成功 - " + materialCounts);
            } catch (Exception e) {
                System.err.println("ERROR: 素材所持数の取得に失敗しました - " + e.getMessage());
                e.printStackTrace();
            }

            // モデルに追加
            model.addAttribute("materialCounts", materialCounts);
            model.addAttribute("userLevel", user.getLevel());
            model.addAttribute("unlockedIds", unlockedIds);
            model.addAttribute("fireChars", fireChars);
            model.addAttribute("waterChars", waterChars);
            model.addAttribute("grassChars", grassChars);
            model.addAttribute("lightChars", lightChars);
            model.addAttribute("darkChars", darkChars);
            model.addAttribute("secretChars", secretChars);

            System.out.println("DEBUG: Model attributes added successfully");
            
            return "characters/CharactersUnlock";
            
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR in showUnlockPage: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("materialCounts", materialCounts);
            model.addAttribute("userLevel", 1);
            model.addAttribute("unlockedIds", new HashSet<>());
            model.addAttribute("fireChars", new ArrayList<>());
            model.addAttribute("waterChars", new ArrayList<>());
            model.addAttribute("grassChars", new ArrayList<>());
            model.addAttribute("lightChars", new ArrayList<>());
            model.addAttribute("darkChars", new ArrayList<>());
            model.addAttribute("secretChars", new ArrayList<>());
            
            return "characters/CharactersUnlock";
        }
    }

    
    }