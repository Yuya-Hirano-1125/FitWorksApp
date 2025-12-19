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
@RequestMapping("/characters")
public class CharactersUnlockViewController {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CharacterService characterService;

    @GetMapping("/unlock")
    public String showUnlockPage(Model model, Principal principal) {
        // キーはアイテムID(String)、値は所持数
        Map<String, Integer> materialCounts = new HashMap<>();
        
        // data.sql に登録されているアイテムID 1〜16 を初期化
        for (int i = 1; i <= 16; i++) {
            materialCounts.put(String.valueOf(i), 0);
        }

        String username = principal.getName();
        
        try {
            // ID指定でDBから所持数を取得
            // ※UserServiceのgetUserMaterialCountは、行数ではなく個数(quantity)を返すように修正されている前提
            for (int i = 1; i <= 16; i++) {
                Long itemId = (long) i;
                int count = userService.getUserMaterialCount(username, itemId);
                materialCounts.put(String.valueOf(i), count);
            }
            System.out.println("DEBUG: User " + username + " Materials (ID 1-16): " + materialCounts);
        } catch (Exception e) {
            System.err.println("ERROR: 素材所持数の取得に失敗しました: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return setupEmptyModel(model, materialCounts);
            }

            // 各属性のキャラクターを取得
            List<CharacterEntity> fireChars = characterRepository.findByAttribute("fire");
            List<CharacterEntity> waterChars = characterRepository.findByAttribute("water");
            List<CharacterEntity> grassChars = characterRepository.findByAttribute("grass");
            List<CharacterEntity> lightChars = characterRepository.findByAttribute("light");
            List<CharacterEntity> darkChars = characterRepository.findByAttribute("dark");
            List<CharacterEntity> secretChars = characterRepository.findByAttribute("secret");

            // 進化データを適用
            applyEvolutionDataSafe(fireChars);
            applyEvolutionDataSafe(waterChars);
            applyEvolutionDataSafe(grassChars);
            applyEvolutionDataSafe(lightChars);
            applyEvolutionDataSafe(darkChars);
            applyEvolutionDataSafe(secretChars);

            Set<Long> unlockedIds = user.getUnlockedCharacters();
            if (unlockedIds == null) {
                unlockedIds = new HashSet<>();
            }

            model.addAttribute("materialCounts", materialCounts);
            model.addAttribute("userLevel", user.getLevel());
            model.addAttribute("unlockedIds", unlockedIds);
            model.addAttribute("fireChars", fireChars);
            model.addAttribute("waterChars", waterChars);
            model.addAttribute("grassChars", grassChars);
            model.addAttribute("lightChars", lightChars);
            model.addAttribute("darkChars", darkChars);
            model.addAttribute("secretChars", secretChars);
            
            return "characters/CharactersUnlock";
            
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR in showUnlockPage: " + e.getMessage());
            e.printStackTrace();
            return setupEmptyModel(model, materialCounts);
        }
    }
    
    private void applyEvolutionDataSafe(List<CharacterEntity> chars) {
        if (chars == null) return;
        for (CharacterEntity chara : chars) {
            characterService.applyEvolutionData(chara);
        }
    }

    private String setupEmptyModel(Model model, Map<String, Integer> counts) {
        model.addAttribute("materialCounts", counts);
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