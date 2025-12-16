package com.example.demo.controller;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.CharacterEntity;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.service.UserService;

@Controller
public class CharacterUnlockController {

    private final CharacterRepository repository;
    private final UserService userService;

    public CharacterUnlockController(CharacterRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    /**
     * キャラクター解放処理
     */
    @PostMapping("/characters/unlock")
    public String unlockCharacter(@RequestParam Long characterId,
                                  @RequestParam int cost,
                                  @RequestParam String materialType,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal) {

        Optional<CharacterEntity> optChara = repository.findById(characterId);
        if (optChara.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "対象キャラクターが存在しません。");
            return "redirect:/characters";
        }

        CharacterEntity chara = optChara.get();

        // ===== 進化素材 & 進化条件構築 =====
        switch (chara.getName()) {
            case "ドラコ":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("紅玉", 3),
                    Map.entry("蒼玉", 3),
                    Map.entry("翠玉", 3),
                    Map.entry("聖玉", 3),
                    Map.entry("闇玉", 3)
                ));
                chara.setEvolutionConditions(Map.ofEntries(
                    Map.entry("必要レベル", "10"),
                    Map.entry("素材ランク", "R素材")
                ));
                break;

            case "ドラコス":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("紅玉", 5),
                    Map.entry("蒼玉", 5),
                    Map.entry("翠玉", 5),
                    Map.entry("聖玉", 5),
                    Map.entry("闇玉", 5),
                    Map.entry("赤の聖結晶", 3),
                    Map.entry("青の聖結晶", 3),
                    Map.entry("緑の聖結晶", 3),
                    Map.entry("黄の聖結晶", 3),
                    Map.entry("闇の聖結晶", 3)
                ));
                chara.setEvolutionConditions(Map.ofEntries(
                    Map.entry("必要レベル", "20"),
                    Map.entry("素材ランク", "R素材 + SR素材"),
                    Map.entry("必要キャラ解放", "ドラコ")
                ));
                break;

            case "ドラグノイド":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("紅玉", 7),
                    Map.entry("蒼玉", 7),
                    Map.entry("翠玉", 7),
                    Map.entry("聖玉", 7),
                    Map.entry("闇玉", 7),
                    Map.entry("赤の聖結晶", 5),
                    Map.entry("青の聖結晶", 5),
                    Map.entry("緑の聖結晶", 5),
                    Map.entry("黄の聖結晶", 5),
                    Map.entry("闇の聖結晶", 5),
                    Map.entry("赫焔鱗", 1)
                ));
                chara.setEvolutionConditions(Map.ofEntries(
                    Map.entry("必要レベル", "30"),
                    Map.entry("素材ランク", "R素材 + SR素材 + SSR素材"),
                    Map.entry("属性限定", "火属性のみ"),
                    Map.entry("必要キャラ解放", "ドラコス")
                ));
                break;

            default:
                chara.setEvolutionMaterials(Map.of()); // エンバーハートなど素材不要
                chara.setEvolutionConditions(Map.of());
        }

        // ===== デバッグログ =====
        System.out.println("DEBUG: ImagePath=" + chara.getImagePath());
        System.out.println("DEBUG: EvolutionMaterials=" + chara.getEvolutionMaterials());
        System.out.println("DEBUG: EvolutionConditions=" + chara.getEvolutionConditions());

        // --- ユーザー情報を取得 ---
        String username = principal.getName();
        int userLevel = userService.getUserLevel(username);
        int userMaterialCount = userService.getUserMaterialCount(username, materialType);

        boolean canUnlock = (userLevel >= chara.getRequiredLevel() && userMaterialCount >= cost);

        if (canUnlock) {
            userService.consumeUserMaterial(username, materialType, cost);
            userService.unlockCharacterForUser(username, chara.getId());

            redirectAttributes.addFlashAttribute(
                "message",
                String.format("%s を解放しました！ (必要Lv:%d / 必要素材:%d)",
                        chara.getName(),
                        chara.getRequiredLevel(),
                        cost)
            );
        } else {
            redirectAttributes.addFlashAttribute(
                "error",
                String.format("条件不足です！ %s の解放には Lv.%d 以上と素材 %d 個が必要です。",
                        chara.getName(),
                        chara.getRequiredLevel(),
                        cost)
            );
        }

        return "redirect:/characters";
    }
}
