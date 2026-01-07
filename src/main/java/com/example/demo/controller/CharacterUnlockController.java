package com.example.demo.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.CharacterEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.service.CharacterService;
import com.example.demo.service.UserService;

@RestController
public class CharacterUnlockController {

    private final CharacterRepository repository;
    private final UserService userService;
    private final CharacterService characterService;

    public CharacterUnlockController(CharacterRepository repository,
                                     UserService userService,
                                     CharacterService characterService) {
        this.repository = repository;
        this.userService = userService;
        this.characterService = characterService;
    }

    @PostMapping("/unlock")
    public ResponseEntity<Map<String, Object>> unlockCharacter(@RequestBody Map<String, Object> request,
                                                               Principal principal) {

        System.out.println("========== 解放処理開始 ==========");

        // ID取得
        Long characterId;
        try {
            characterId = Long.valueOf(request.get("characterId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "無効なリクエスト"));
        }
        System.out.println("解放対象キャラID: " + characterId);

        // キャラクター存在チェック
        Optional<CharacterEntity> optChara = repository.findById(characterId);
        if (optChara.isEmpty()) return ResponseEntity.badRequest().body(Map.of("success", false));
        CharacterEntity chara = optChara.get();
        System.out.println("解放対象キャラ名: " + chara.getName());

        // ユーザー取得
        User user = userService.findByUsername(principal.getName());
        if (user == null) return ResponseEntity.badRequest().body(Map.of("success", false));
        
        // ★ログ: 処理前の選択中IDを確認
        System.out.println("【処理前】ユーザーの選択中キャラID: " + user.getSelectedCharacterId());

        // 進化条件セット
        characterService.applyEvolutionData(chara);
        Map<String, Integer> requiredMaterials = chara.getEvolutionMaterials();
        Map<String, String> conditions = chara.getEvolutionConditions();
        List<String> failureReasons = new ArrayList<>();

        // (中略: チェック処理は変更なし)
        // 1. レベルチェック
        if (user.getLevel() < chara.getRequiredLevel()) failureReasons.add("レベル不足");
        
        // 2. 素材チェック
        for (Map.Entry<String, Integer> entry : requiredMaterials.entrySet()) {
            String matName = entry.getKey();
            Long matItemId;
            try { matItemId = Long.parseLong(matName); } catch (Exception e) { matItemId = characterService.getMaterialItemId(matName); }
            int required = entry.getValue();
            int userCount = userService.getUserMaterialCount(user.getUsername(), matItemId);
            if (userCount < required) failureReasons.add("素材不足");
        }
        
        // 3. 前提キャラチェック
        if (conditions.containsKey("前提キャラ")) {
            String prereqName = conditions.get("前提キャラ");
            Long prereqId = getCharacterIdByName(prereqName);
            if (prereqId != null && !user.hasUnlockedCharacter(prereqId)) failureReasons.add("前提キャラ未解放");
        }

        Map<String, Object> response = new HashMap<>();
        
        if (failureReasons.isEmpty()) {
            // === 解放成功 ===
            System.out.println(">>> 条件クリア。解放処理を実行します。");

            Map<String, Integer> newMaterialCounts = new HashMap<>();
            for (Map.Entry<String, Integer> entry : requiredMaterials.entrySet()) {
                String matKey = entry.getKey();
                Long matItemId;
                try { matItemId = Long.parseLong(matKey); } catch (Exception e) { matItemId = characterService.getMaterialItemId(matKey); }
                userService.consumeUserMaterialByItemId(user.getUsername(), matItemId, entry.getValue());
                newMaterialCounts.put(matKey, userService.getUserMaterialCount(user.getUsername(), matItemId));
            }
            
            // ★重要: ここでServiceを呼んで保存
            userService.unlockCharacterForUser(user.getUsername(), chara.getId());
            
            // ★ログ: 保存直後の状態を確認するために再取得してみる
            User userAfter = userService.findByUsername(principal.getName());
            System.out.println("【保存後】DB上の選択中キャラID: " + userAfter.getSelectedCharacterId());

            if (userAfter.getSelectedCharacterId() != null && userAfter.getSelectedCharacterId().equals(chara.getId())) {
                System.err.println("!!! 警告: 選択中IDが解放キャラID(" + chara.getId() + ")に変わっています！ !!!");
            } else {
                System.out.println("正常: 選択中IDは変更されていません。");
            }

            response.put("success", true);
            response.put("message", chara.getName() + " を解放しました!");
            response.put("newMaterialCounts", newMaterialCounts);
            
        } else {
            // 失敗
            System.out.println("解放失敗: " + failureReasons);
            response.put("success", false);
            response.put("message", "条件を満たしていません");
        }
        
        System.out.println("========== 解放処理終了 ==========");
        return ResponseEntity.ok(response);
    }
    
    private Long getCharacterIdByName(String name) {
        return repository.findAll().stream()
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .map(CharacterEntity::getId)
            .orElse(null);
    }
}