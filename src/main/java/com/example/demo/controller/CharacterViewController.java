package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.CharacterEntity;
import com.example.demo.repository.CharacterRepository;

@Controller
public class CharacterViewController {

    private final CharacterRepository repository;

    public CharacterViewController(CharacterRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/characters")
    public String showCharacters(Model model) {
        // 全キャラクター一覧を取得
        List<CharacterEntity> characters = repository.findAll();

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
                ", ImagePath=" + c.getImagePath()
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

        // 闇属性は4体まで表示（シークレット除外）
        model.addAttribute("darkChars", characters.stream()
                .filter(c -> "dark".equals(c.getAttribute()))
                .filter(c -> !"シークレット".equals(c.getName())) // 名前がシークレットなら除外
                .limit(4)
                .toList());

        // シークレット属性は "secret" または名前がシークレットのものを表示
        model.addAttribute("secretChars", characters.stream()
                .filter(c -> "secret".equals(c.getAttribute()) || "シークレット".equals(c.getName()))
                .toList());

        // 仮のユーザーデータ
        model.addAttribute("userLevel", 50);
        model.addAttribute("userMaterials", Map.of(
                "fire", 20,
                "water", 15,
                "grass", 10,
                "light", 5,
                "dark", 8,
                "secret", 1
        ));
        model.addAttribute("unlockedIds", List.of(0L, 10L, 40L));

        // CharactersUnlock.html を返す
        return "characters/menu/CharactersUnlock";
    }
}
