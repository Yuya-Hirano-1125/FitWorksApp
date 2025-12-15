package com.example.demo.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.CharacterEntity;
import com.example.demo.repository.CharacterRepository;

@Controller
public class CharacterUnlockController {

    private final CharacterRepository repository;

    public CharacterUnlockController(CharacterRepository repository) {
        this.repository = repository;
    }

    /**
     * キャラクター進化処理
     */
    @PostMapping("/characters/unlock")
    public String unlockCharacter(@RequestParam Long characterId,
                                  @RequestParam int cost,
                                  @RequestParam String materialType,
                                  RedirectAttributes redirectAttributes) {

        // DBから対象キャラクターを取得
        Optional<CharacterEntity> optChara = repository.findById(characterId);
        if (optChara.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "対象キャラクターが存在しません。");
            return "redirect:/characters";
        }

        CharacterEntity chara = optChara.get();

        // ===== デバッグログで画像パス確認 =====
        System.out.println("DEBUG: ImagePath=" + chara.getImagePath());

        // TODO: ユーザー情報を取得して素材やレベルをチェックする処理を追加
        int userLevel = getDummyUserLevel();
        int userMaterialCount = getDummyUserMaterialCount(materialType);

        // 判定処理
        boolean canUnlock = (userLevel >= chara.getRequiredLevel() && userMaterialCount >= cost);

        if (canUnlock) {
            // TODO: 実際にはユーザーの素材を減算し、キャラを解放済みに更新する処理を追加
            redirectAttributes.addFlashAttribute(
                "message",
                String.format("%s を進化しました！ (必要Lv:%d / 必要素材:%d)",
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

        // 一覧画面にリダイレクト
        return "redirect:/characters";
    }

    /**
     * 仮のユーザーレベル取得（後でUserEntityに置き換え）
     */
    private int getDummyUserLevel() {
        return 50;
    }

    /**
     * 仮の素材数取得（後でUserEntityに置き換え）
     */
    private int getDummyUserMaterialCount(String materialType) {
        switch (materialType) {
            case "fire": return 20;
            case "water": return 15;
            case "grass": return 10;
            case "light": return 5;
            case "dark": return 8;
            case "secret": return 1;
            default: return 0;
        }
    }
}
