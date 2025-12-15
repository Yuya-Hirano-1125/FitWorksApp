package com.example.demo.controller;

import java.security.Principal;
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
     * キャラクター進化処理
     */
    @PostMapping("/characters/unlock")
    public String unlockCharacter(@RequestParam Long characterId,
                                  @RequestParam int cost,
                                  @RequestParam String materialType,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal) {

        // DBから対象キャラクターを取得
        Optional<CharacterEntity> optChara = repository.findById(characterId);
        if (optChara.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "対象キャラクターが存在しません。");
            return "redirect:/characters";
        }

        CharacterEntity chara = optChara.get();

        // ===== デバッグログ =====
        System.out.println("DEBUG: ImagePath=" + chara.getImagePath());

        // --- ユーザー情報をDBから取得 ---
        String username = principal.getName();
        int userLevel = userService.getUserLevel(username); // DBからレベル取得
        int userMaterialCount = userService.getUserMaterialCount(username, materialType); // DBから素材数取得

        // 判定処理
        boolean canUnlock = (userLevel >= chara.getRequiredLevel() && userMaterialCount >= cost);

        if (canUnlock) {
            // TODO: 素材を減算し、キャラを解放済みに更新する処理を追加
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
}
