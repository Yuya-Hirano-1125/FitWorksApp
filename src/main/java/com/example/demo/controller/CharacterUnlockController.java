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
    private final CharacterService characterService; // 追加: 共通ロジック用

    public CharacterUnlockController(CharacterRepository repository,
                                     UserService userService,
                                     CharacterService characterService) {
        this.repository = repository;
        this.userService = userService;
        this.characterService = characterService;
    }

    /**
     * キャラクター解放処理 (JSONレスポンス)
     */
    @PostMapping("/unlock")
    public ResponseEntity<Map<String, Object>> unlockCharacter(@RequestBody Map<String, Object> request,
                                                               Principal principal) {

        // リクエストからIDを取得
        Long characterId;
        try {
            characterId = Long.valueOf(request.get("characterId").toString());
        } catch (NumberFormatException | NullPointerException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "無効なリクエストです。"));
        }

        // キャラクター存在チェック
        Optional<CharacterEntity> optChara = repository.findById(characterId);
        if (optChara.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "対象キャラクターが存在しません。"));
        }

        CharacterEntity chara = optChara.get();
        
        // ユーザー取得
        User user = userService.findByUsername(principal.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ユーザーが見つかりません。"));
        }

        // ★重要: Serviceを使って正しい進化条件・素材情報をセット
        // (これにより、画面表示時と同じロジックで判定が行われます)
        characterService.applyEvolutionData(chara);
        
        Map<String, Integer> requiredMaterials = chara.getEvolutionMaterials();
        Map<String, String> conditions = chara.getEvolutionConditions();

        List<String> failureReasons = new ArrayList<>();
        
        // ==========================================
        // 1. レベルチェック
        // ==========================================
        if (user.getLevel() < chara.getRequiredLevel()) {
            failureReasons.add("レベル不足 (必要: Lv." + chara.getRequiredLevel() + ")");
        }
        
        // ==========================================
        // 2. 素材チェック
        // ==========================================
        for (Map.Entry<String, Integer> entry : requiredMaterials.entrySet()) {
            String matName = entry.getKey(); // ここでのキーは素材名ではなくID文字列("1"など)の場合と、名前の場合があるため注意
            // CharacterServiceの実装ではキーをアイテムID("1", "2"...)としてセットしていますが、
            // もし名前が入っていてもIDに変換できるようにService側で吸収するか、ここでハンドリングします。
            
            // 今回のCharacterService実装ではキーが"アイテムID文字列"になっている想定で進めます
            Long matItemId;
            String displayName = matName; // エラー表示用
            
            try {
                // キーが数字ならそのままIDとして使う
                matItemId = Long.parseLong(matName);
                // 表示用に名前を引くなどの処理ができればベストですが、ここでは簡易的にID表示またはServiceのヘルパーを利用
                // (CharacterServiceにgetNameByIdがあると親切ですが、今回はそのままIDで処理)
            } catch (NumberFormatException e) {
                // キーが名前("紅玉"など)の場合はID変換
                matItemId = characterService.getMaterialItemId(matName);
            }

            int required = entry.getValue();
            int userCount = userService.getUserMaterialCount(user.getUsername(), matItemId);
            
            if (userCount < required) {
                // ユーザーにはわかりやすい名前で不足を通知したい場合、別途マッピングが必要ですが
                // ここでは簡易メッセージとします
                failureReasons.add("素材(ID:" + matName + ") が不足しています (所持: " + userCount + "/" + required + ")");
            }
        }
        
        // ==========================================
        // 3. 前提キャラクターチェック
        // ==========================================
        if (conditions.containsKey("前提キャラ")) {
            String prereqName = conditions.get("前提キャラ");
            Long prereqId = getCharacterIdByName(prereqName);
            
            if (prereqId == null) {
                 // DBに前提キャラが見つからない場合（設定ミス等）
                 System.err.println("WARNING: 前提キャラ '" + prereqName + "' がDBに見つかりません。");
            } else if (!user.hasUnlockedCharacter(prereqId)) {
                failureReasons.add("前提キャラ「" + prereqName + "」が未解放です");
            }
        }

        // ==========================================
        // 結果判定
        // ==========================================
        Map<String, Object> response = new HashMap<>();
        
        if (failureReasons.isEmpty()) {
            // ===== 解放成功: 素材消費とアンロック =====
        	
        	// ★追加: 最新の所持数を格納するマップ
            Map<String, Integer> newMaterialCounts = new HashMap<>();
            
            for (Map.Entry<String, Integer> entry : requiredMaterials.entrySet()) {
                String matKey = entry.getKey();
                int amount = entry.getValue();
                
                Long matItemId;
                try {
                    matItemId = Long.parseLong(matKey);
                } catch (NumberFormatException e) {
                    matItemId = characterService.getMaterialItemId(matKey);
                }
                
                userService.consumeUserMaterialByItemId(user.getUsername(), matItemId, amount);
                
             // ★追加: 消費後の最新所持数を取得してマップに格納
                // (matKeyはフロントエンドのmaterialCountsのキーと一致させるためそのまま使用)
                int currentAmount = userService.getUserMaterialCount(user.getUsername(), matItemId);
                newMaterialCounts.put(matKey, currentAmount);
            }
            
            // キャラクター解放
            userService.unlockCharacterForUser(user.getUsername(), chara.getId());
            
            response.put("success", true);
            response.put("message", chara.getName() + " を解放しました!");
         // ★追加: レスポンスに最新の素材数をセット
            response.put("newMaterialCounts", newMaterialCounts);
            
        } else {
            // ===== 解放失敗 =====
            String errorMessage = "条件を満たしていません:\n" + String.join("\n", failureReasons);
            response.put("success", false);
            response.put("message", errorMessage);
        }

        return ResponseEntity.ok(response);
    }
    
    /**
     * キャラクター名からIDを取得するヘルパーメソッド
     */
    private Long getCharacterIdByName(String name) {
        return repository.findAll().stream()
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .map(CharacterEntity::getId)
            .orElse(null);
    }
}