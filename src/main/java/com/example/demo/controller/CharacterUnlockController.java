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
import com.example.demo.service.UserService;

@RestController
public class CharacterUnlockController {

    private final CharacterRepository repository;
    private final UserService userService;

    public CharacterUnlockController(CharacterRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    /**
     * キャラクター解放処理 (JSONレスポンス)
     */
    @PostMapping("/unlock")
    public ResponseEntity<Map<String, Object>> unlockCharacter(@RequestBody Map<String, Object> request,
                                                               Principal principal) {

        System.out.println("DEBUG: /unlock called with request=" + request);

        Long characterId = Long.valueOf(request.get("characterId").toString());
        int cost = Integer.parseInt(request.get("cost").toString());
        String materialType = request.get("materialType").toString();

        System.out.println("DEBUG: characterId=" + characterId + ", cost=" + cost + ", materialType=" + materialType);

        Optional<CharacterEntity> optChara = repository.findById(characterId);
        if (optChara.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "対象キャラクターが存在しません。"
            ));
        }

        CharacterEntity chara = optChara.get();
        System.out.println("DEBUG: 対象キャラ=" + chara.getName());

        // --- ユーザー情報を取得 ---
        String username = principal.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "ユーザー情報が取得できませんでした。"
            ));
        }

        int userLevel = user.getLevel();
        
        // ===== 進化素材 & 進化条件構築 =====
        Map<String, Integer> evolutionMaterials = new HashMap<>();
        Map<String, String> evolutionConditions = new HashMap<>();
        List<String> prerequisiteCharacters = new ArrayList<>();
        
        // ★★★ 修正: 実際のDB登録キャラクター名に合わせる ★★★
        switch (chara.getName()) {
            // ===== 炎属性 =====
            case "エンバーハート":
                // 初期キャラは素材不要
                evolutionConditions.put("必要レベル", "1");
                break;

            case "ドラコ":
                evolutionMaterials.put("紅玉", 3);
                evolutionMaterials.put("蒼玉", 3);
                evolutionMaterials.put("翠玉", 3);
                evolutionMaterials.put("聖玉", 3);
                evolutionMaterials.put("闇玉", 3);
                evolutionConditions.put("必要レベル", "10");
                break;

            case "ドラコス":
                evolutionMaterials.put("紅玉", 5);
                evolutionMaterials.put("蒼玉", 5);
                evolutionMaterials.put("翠玉", 5);
                evolutionMaterials.put("聖玉", 5);
                evolutionMaterials.put("闇玉", 5);
                evolutionMaterials.put("赤の聖結晶", 3);
                evolutionMaterials.put("青の聖結晶", 3);
                evolutionMaterials.put("緑の聖結晶", 3);
                evolutionMaterials.put("黄の聖結晶", 3);
                evolutionMaterials.put("紫の聖結晶", 3);
                evolutionConditions.put("必要レベル", "20");
                prerequisiteCharacters.add("ドラコ");
                break;

            case "ドラグノイド":
                evolutionMaterials.put("紅玉", 7);
                evolutionMaterials.put("蒼玉", 7);
                evolutionMaterials.put("翠玉", 7);
                evolutionMaterials.put("聖玉", 7);
                evolutionMaterials.put("闇玉", 7);
                evolutionMaterials.put("赤の聖結晶", 5);
                evolutionMaterials.put("青の聖結晶", 5);
                evolutionMaterials.put("緑の聖結晶", 5);
                evolutionMaterials.put("黄の聖結晶", 5);
                evolutionMaterials.put("紫の聖結晶", 5);
                evolutionMaterials.put("赫焔鱗", 1);
                evolutionConditions.put("必要レベル", "30");
                prerequisiteCharacters.add("ドラコス");
                break;

            // ===== 水属性 =====
            case "ルーナドロップ":
                evolutionMaterials.put("紅玉", 3);
                evolutionMaterials.put("蒼玉", 3);
                evolutionMaterials.put("翠玉", 3);
                evolutionMaterials.put("聖玉", 3);
                evolutionMaterials.put("闇玉", 3);
                evolutionConditions.put("必要レベル", "40");
                prerequisiteCharacters.add("ドラグノイド");
                break;

            case "ドリー":
                evolutionMaterials.put("紅玉", 5);
                evolutionMaterials.put("蒼玉", 5);
                evolutionMaterials.put("翠玉", 5);
                evolutionMaterials.put("聖玉", 5);
                evolutionMaterials.put("闇玉", 5);
                evolutionMaterials.put("赤の聖結晶", 3);
                evolutionMaterials.put("青の聖結晶", 3);
                evolutionMaterials.put("緑の聖結晶", 3);
                evolutionMaterials.put("黄の聖結晶", 3);
                evolutionMaterials.put("紫の聖結晶", 3);
                evolutionConditions.put("必要レベル", "50");
                prerequisiteCharacters.add("ルーナドロップ");
                break;

            case "ドルフィ":
                evolutionMaterials.put("紅玉", 7);
                evolutionMaterials.put("蒼玉", 7);
                evolutionMaterials.put("翠玉", 7);
                evolutionMaterials.put("聖玉", 7);
                evolutionMaterials.put("闇玉", 7);
                evolutionMaterials.put("赤の聖結晶", 5);
                evolutionMaterials.put("青の聖結晶", 5);
                evolutionMaterials.put("緑の聖結晶", 5);
                evolutionMaterials.put("黄の聖結晶", 5);
                evolutionMaterials.put("紫の聖結晶", 5);
                evolutionMaterials.put("氷華の杖", 1);
                evolutionConditions.put("必要レベル", "60");
                prerequisiteCharacters.add("ドリー");
                break;

            case "ドルフィナス":
                evolutionMaterials.put("紅玉", 7);
                evolutionMaterials.put("蒼玉", 7);
                evolutionMaterials.put("翠玉", 7);
                evolutionMaterials.put("聖玉", 7);
                evolutionMaterials.put("闇玉", 7);
                evolutionMaterials.put("赤の聖結晶", 5);
                evolutionMaterials.put("青の聖結晶", 5);
                evolutionMaterials.put("緑の聖結晶", 5);
                evolutionMaterials.put("黄の聖結晶", 5);
                evolutionMaterials.put("紫の聖結晶", 5);
                evolutionMaterials.put("氷華の杖", 2);
                evolutionConditions.put("必要レベル", "70");
                prerequisiteCharacters.add("ドルフィ");
                break;

            // ===== 草属性 =====
            case "フォリアン":
                evolutionMaterials.put("紅玉", 3);
                evolutionMaterials.put("蒼玉", 3);
                evolutionMaterials.put("翠玉", 3);
                evolutionMaterials.put("聖玉", 3);
                evolutionMaterials.put("闇玉", 3);
                evolutionConditions.put("必要レベル", "80");
                prerequisiteCharacters.add("ドルフィナス");
                break;

            case "シル":
                evolutionMaterials.put("紅玉", 5);
                evolutionMaterials.put("蒼玉", 5);
                evolutionMaterials.put("翠玉", 5);
                evolutionMaterials.put("聖玉", 5);
                evolutionMaterials.put("闇玉", 5);
                evolutionMaterials.put("赤の聖結晶", 3);
                evolutionMaterials.put("青の聖結晶", 3);
                evolutionMaterials.put("緑の聖結晶", 3);
                evolutionMaterials.put("黄の聖結晶", 3);
                evolutionMaterials.put("紫の聖結晶", 3);
                evolutionConditions.put("必要レベル", "90");
                prerequisiteCharacters.add("フォリアン");
                break;

            case "シルファ":
                evolutionMaterials.put("紅玉", 7);
                evolutionMaterials.put("蒼玉", 7);
                evolutionMaterials.put("翠玉", 7);
                evolutionMaterials.put("聖玉", 7);
                evolutionMaterials.put("闇玉", 7);
                evolutionMaterials.put("赤の聖結晶", 5);
                evolutionMaterials.put("青の聖結晶", 5);
                evolutionMaterials.put("緑の聖結晶", 5);
                evolutionMaterials.put("黄の聖結晶", 5);
                evolutionMaterials.put("紫の聖結晶", 5);
                evolutionMaterials.put("緑晶灯", 1);
                evolutionConditions.put("必要レベル", "100");
                prerequisiteCharacters.add("シル");
                break;

            case "シルフィナ":
                evolutionMaterials.put("紅玉", 7);
                evolutionMaterials.put("蒼玉", 7);
                evolutionMaterials.put("翠玉", 7);
                evolutionMaterials.put("聖玉", 7);
                evolutionMaterials.put("闇玉", 7);
                evolutionMaterials.put("赤の聖結晶", 5);
                evolutionMaterials.put("青の聖結晶", 5);
                evolutionMaterials.put("緑の聖結晶", 5);
                evolutionMaterials.put("黄の聖結晶", 5);
                evolutionMaterials.put("紫の聖結晶", 5);
                evolutionMaterials.put("緑晶灯", 2);
                evolutionConditions.put("必要レベル", "110");
                prerequisiteCharacters.add("シルファ");
                break;

            // ===== 光属性 =====
            case "ハローネスト":
                evolutionMaterials.put("紅玉", 3);
                evolutionMaterials.put("蒼玉", 3);
                evolutionMaterials.put("翠玉", 3);
                evolutionMaterials.put("聖玉", 3);
                evolutionMaterials.put("闇玉", 3);
                evolutionConditions.put("必要レベル", "120");
                prerequisiteCharacters.add("シルフィナ");
                break;

            case "メリー":
                evolutionMaterials.put("紅玉", 5);
                evolutionMaterials.put("蒼玉", 5);
                evolutionMaterials.put("翠玉", 5);
                evolutionMaterials.put("聖玉", 5);
                evolutionMaterials.put("闇玉", 5);
                evolutionMaterials.put("赤の聖結晶", 3);
                evolutionMaterials.put("青の聖結晶", 3);
                evolutionMaterials.put("緑の聖結晶", 3);
                evolutionMaterials.put("黄の聖結晶", 3);
                evolutionMaterials.put("紫の聖結晶", 3);
                evolutionConditions.put("必要レベル", "130");
                prerequisiteCharacters.add("ハローネスト");
                break;

            case "メリル":
                evolutionMaterials.put("紅玉", 7);
                evolutionMaterials.put("蒼玉", 7);
                evolutionMaterials.put("翠玉", 7);
                evolutionMaterials.put("聖玉", 7);
                evolutionMaterials.put("闇玉", 7);
                evolutionMaterials.put("赤の聖結晶", 5);
                evolutionMaterials.put("青の聖結晶", 5);
                evolutionMaterials.put("緑の聖結晶", 5);
                evolutionMaterials.put("黄の聖結晶", 5);
                evolutionMaterials.put("紫の聖結晶", 5);
                evolutionMaterials.put("夢紡ぎの枕", 1);
                evolutionConditions.put("必要レベル", "140");
                prerequisiteCharacters.add("メリー");
                break;

            case "メリノア":
                evolutionMaterials.put("紅玉", 7);
                evolutionMaterials.put("蒼玉", 7);
                evolutionMaterials.put("翠玉", 7);
                evolutionMaterials.put("聖玉", 7);
                evolutionMaterials.put("闇玉", 7);
                evolutionMaterials.put("赤の聖結晶", 5);
                evolutionMaterials.put("青の聖結晶", 5);
                evolutionMaterials.put("緑の聖結晶", 5);
                evolutionMaterials.put("黄の聖結晶", 5);
                evolutionMaterials.put("紫の聖結晶", 5);
                evolutionMaterials.put("夢紡ぎの枕", 2);
                evolutionConditions.put("必要レベル", "150");
                prerequisiteCharacters.add("メリル");
                break;

            // ===== 闇属性 =====
            case "ネビュリス":
                evolutionMaterials.put("紅玉", 3);
                evolutionMaterials.put("蒼玉", 3);
                evolutionMaterials.put("翠玉", 3);
                evolutionMaterials.put("聖玉", 3);
                evolutionMaterials.put("闇玉", 3);
                evolutionConditions.put("必要レベル", "160");
                prerequisiteCharacters.add("メリノア");
                break;

            case "ロービ":
                evolutionMaterials.put("紅玉", 5);
                evolutionMaterials.put("蒼玉", 5);
                evolutionMaterials.put("翠玉", 5);
                evolutionMaterials.put("聖玉", 5);
                evolutionMaterials.put("闇玉", 5);
                evolutionMaterials.put("赤の聖結晶", 3);
                evolutionMaterials.put("青の聖結晶", 3);
                evolutionMaterials.put("緑の聖結晶", 3);
                evolutionMaterials.put("黄の聖結晶", 3);
                evolutionMaterials.put("紫の聖結晶", 3);
                evolutionConditions.put("必要レベル", "170");
                prerequisiteCharacters.add("ネビュリス");
                break;

            case "ローバス":
                evolutionMaterials.put("紅玉", 7);
                evolutionMaterials.put("蒼玉", 7);
                evolutionMaterials.put("翠玉", 7);
                evolutionMaterials.put("聖玉", 7);
                evolutionMaterials.put("闇玉", 7);
                evolutionMaterials.put("赤の聖結晶", 5);
                evolutionMaterials.put("青の聖結晶", 5);
                evolutionMaterials.put("緑の聖結晶", 5);
                evolutionMaterials.put("黄の聖結晶", 5);
                evolutionMaterials.put("紫の聖結晶", 5);
                evolutionMaterials.put("月詠みの杖", 1);
                evolutionConditions.put("必要レベル", "180");
                prerequisiteCharacters.add("ロービ");
                break;

            case "ロービアス":
                evolutionMaterials.put("紅玉", 7);
                evolutionMaterials.put("蒼玉", 7);
                evolutionMaterials.put("翠玉", 7);
                evolutionMaterials.put("聖玉", 7);
                evolutionMaterials.put("闇玉", 7);
                evolutionMaterials.put("赤の聖結晶", 5);
                evolutionMaterials.put("青の聖結晶", 5);
                evolutionMaterials.put("緑の聖結晶", 5);
                evolutionMaterials.put("黄の聖結晶", 5);
                evolutionMaterials.put("紫の聖結晶", 5);
                evolutionMaterials.put("月詠みの杖", 2);
                evolutionConditions.put("必要レベル", "190");
                prerequisiteCharacters.add("ローバス");
                break;

            // ===== シークレット属性 =====
            case "シークレット":
                evolutionMaterials.put("虹玉", 15);
                evolutionMaterials.put("紅玉", 10);
                evolutionMaterials.put("蒼玉", 10);
                evolutionMaterials.put("翠玉", 10);
                evolutionMaterials.put("聖玉", 10);
                evolutionMaterials.put("闇玉", 10);
                evolutionMaterials.put("赤の聖結晶", 5);
                evolutionMaterials.put("青の聖結晶", 5);
                evolutionMaterials.put("緑の聖結晶", 5);
                evolutionMaterials.put("黄の聖結晶", 5);
                evolutionMaterials.put("闇の聖結晶", 5);
                evolutionConditions.put("必要レベル", "250");
                break;

            default:
                break;
        }
        
        chara.setEvolutionMaterials(evolutionMaterials);
        chara.setEvolutionConditions(evolutionConditions);

        // ===== 解放条件チェック =====
        List<String> failureReasons = new ArrayList<>();
        
        // 1. レベルチェック
        if (userLevel < chara.getRequiredLevel()) {
            failureReasons.add(String.format("レベル不足 (必要: Lv.%d, 現在: Lv.%d)", 
                chara.getRequiredLevel(), userLevel));
        }
        
        // 2. 素材チェック（全ての必要素材を確認）
        for (Map.Entry<String, Integer> entry : evolutionMaterials.entrySet()) {
            String matName = entry.getKey();
            int required = entry.getValue();
            Long matItemId = getMaterialItemId(matName);
            int userCount = userService.getUserMaterialCount(username, matItemId);
            
            System.out.println("DEBUG: 素材チェック - " + matName + " 必要:" + required + " 所持:" + userCount);
            
            if (userCount < required) {
                failureReasons.add(String.format("%s が不足 (必要: %d個, 所持: %d個)", 
                    matName, required, userCount));
            }
        }
        
        // 3. 前提キャラクター解放チェック
        for (String prereqName : prerequisiteCharacters) {
            Long prereqId = getCharacterIdByName(prereqName);
            if (prereqId == null || !user.hasUnlockedCharacter(prereqId)) {
                failureReasons.add(String.format("前提キャラ「%s」が未解放", prereqName));
            }
        }

        Map<String, Object> response = new HashMap<>();
        
        if (failureReasons.isEmpty()) {
            // ===== 解放成功 =====
            // 全ての素材を消費
            for (Map.Entry<String, Integer> entry : evolutionMaterials.entrySet()) {
                String matName = entry.getKey();
                int required = entry.getValue();
                Long matItemId = getMaterialItemId(matName);
                userService.consumeUserMaterialByItemId(username, matItemId, required);
                System.out.println("DEBUG: 素材消費 - " + matName + " x" + required);
            }
            
            // キャラクター解放
            userService.unlockCharacterForUser(username, chara.getId());
            System.out.println("DEBUG: 解放成功! user=" + username + ", charaId=" + chara.getId());

            response.put("success", true);
            response.put("message", String.format("%s を解放しました!", chara.getName()));
        } else {
            // ===== 解放失敗 =====
            System.out.println("DEBUG: 解放失敗 条件不足 user=" + username + ", charaId=" + chara.getId());
            
            String errorMessage = "解放失敗: " + chara.getName() + " の解放には以下が必要です。\n";
            for (String reason : failureReasons) {
                errorMessage += "• " + reason + "\n";
            }
            
            response.put("success", false);
            response.put("message", errorMessage.trim());
        }

        return ResponseEntity.ok(response);
    }
    
    /**
     * 素材タイプ名からitem_idを取得するヘルパーメソッド
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
            case "闇の聖結晶": return 10L;
            case "赫焔鱗": return 11L;
            default: return 1L;
        }
    }
    
    /**
     * キャラクター名からIDを取得するヘルパーメソッド
     */
    private Long getCharacterIdByName(String name) {
        Optional<CharacterEntity> chara = repository.findAll().stream()
            .filter(c -> c.getName().equals(name))
            .findFirst();
        return chara.map(CharacterEntity::getId).orElse(null);
    }
}