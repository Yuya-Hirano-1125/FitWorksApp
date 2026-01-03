package com.example.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.CharacterEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.repository.UserItemRepository;
import com.example.demo.service.UserService;

@Controller
public class CharactersMenuController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserItemRepository userItemRepository;

    // ★追加: キャラクター情報を取得するために必要です
    @Autowired
    private CharacterRepository characterRepository;

    // 夢幻の鍵のアイテムID
    private static final Long DREAM_KEY_ITEM_ID = 16L;

    // ホームのキャラクターボタンからキャラクターメニューへ遷移
    @GetMapping("/characters/menu/CharactersMenu")
    public String showCharactersMenu() {
        return "characters/menu/CharactersMenu"; 
    }

 // キャラクター一覧画面へ遷移
    @GetMapping("/characters/menu/CharactersStorage")
    public String showCharactersStorage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findByUsername(userDetails.getUsername());
        
        List<CharacterEntity> allCharacters = characterRepository.findAll(); 

        List<Map<String, Object>> displayList = new ArrayList<>();
        
        Long selectedId = user.getSelectedCharacterId();
        
     // ★追加: デフォルト(未選択)時に表示されているはずの画像パスを計算
        String defaultImgPath = "/img/character/" + ((user.getLevel() / 10) * 10) + ".png";

        for (CharacterEntity chara : allCharacters) {
            // 解放済みかチェック (所持リストに含まれている か 初期キャラ(ID=0)なら解放)
            boolean isUnlocked = user.hasUnlockedCharacter(chara.getId()) || (chara.getId() == 0); 
            
            // ★追加: 未所持の場合はリストに追加せずスキップする
            if (!isUnlocked) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", chara.getId());
            map.put("name", chara.getName());
            map.put("attribute", chara.getAttribute());
            map.put("imgUrl", chara.getImagePath());
            map.put("requiredLevel", chara.getRequiredLevel());
            map.put("rarity", chara.getRarity());

            map.put("isUnlocked", true); // ここに来る時点で必ずtrue
            
         // ★修正: 選択中かどうかの判定ロジック
            boolean isSelected = false;
            
            if (selectedId != null) {
                // ユーザーが明示的に選択している場合、IDで判定
                isSelected = selectedId.equals(chara.getId());
            } else {
                // まだ選択していない場合、デフォルト画像パスと一致するキャラを選択中とみなす
                if (chara.getImagePath() != null && chara.getImagePath().equals(defaultImgPath)) {
                    isSelected = true;
                }
            }
            
            map.put("isSelected", isSelected);

            displayList.add(map);
        }

        model.addAttribute("charaList", displayList);
        model.addAttribute("currentLevel", user.getLevel());

        return "characters/menu/CharactersStorage"; 
    }

    /**
     * キャラクター選択API (この機能も追加が必要です)
     */
    @PostMapping("/characters/select")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> selectCharacter(
            @RequestBody Map<String, Long> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "ログインが必要です");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long characterId = request.get("characterId");
            User user = userService.findByUsername(userDetails.getUsername());

            // 所持チェック (初期キャラID=0は常にOK、それ以外は所持確認)
            if (characterId != 0 && !user.hasUnlockedCharacter(characterId)) {
                response.put("success", false);
                response.put("message", "このキャラクターはまだ解放されていません。");
                return ResponseEntity.ok(response);
            }

            // 選択中のキャラクターIDを保存
            user.setSelectedCharacterId(characterId);
            userService.save(user);

            response.put("success", true);
            response.put("message", "ホームキャラクターを変更しました！");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 背景一覧画面へ遷移
    @GetMapping("/characters/menu/Backgrounds")
    public String showBackgrounds(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        int userLevel = 1;
        int dreamKeyCount = 0;
        Set<String> unlockedBackgrounds = new HashSet<>();
        String selectedBackground = "fire-original";
        
        if (userDetails != null) {
            String username = userDetails.getUsername();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser != null) {
                userLevel = currentUser.getLevel();
                // 夢幻の鍵の所持数を取得
                dreamKeyCount = userService.getUserMaterialCount(username, DREAM_KEY_ITEM_ID);
                // 解放済み背景リストを取得
                unlockedBackgrounds = currentUser.getUnlockedBackgrounds();
                // 選択中の背景を取得
                selectedBackground = currentUser.getSelectedBackground();
                if (selectedBackground == null || selectedBackground.isEmpty()) {
                    selectedBackground = "fire-original";
                }
            }
        }
        
        model.addAttribute("userLevel", userLevel);
        model.addAttribute("dreamKeyCount", dreamKeyCount);
        model.addAttribute("unlockedBackgrounds", unlockedBackgrounds);
        model.addAttribute("selectedBackground", selectedBackground);
        
        return "characters/menu/Backgrounds"; 
    }

    /**
     * 背景解放APIエンドポイント
     */
    @PostMapping("/characters/backgrounds/unlock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unlockBackground(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "ログインが必要です");
                return ResponseEntity.status(401).body(response);
            }

            String username = userDetails.getUsername();
            String backgroundId = (String) request.get("backgroundId");

            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "ユーザーが見つかりません");
                return ResponseEntity.status(404).body(response);
            }

            // 既に解放済みかチェック
            if (currentUser.hasUnlockedBackground(backgroundId)) {
                response.put("success", false);
                response.put("message", "この背景は既に解放されています");
                return ResponseEntity.ok(response);
            }

            // 所持数チェック
            int dreamKeyCount = userService.getUserMaterialCount(username, DREAM_KEY_ITEM_ID);
            
            if (dreamKeyCount <= 0) {
                response.put("success", false);
                response.put("message", "夢幻の鍵が足りません");
                return ResponseEntity.ok(response);
            }

            // 夢幻の鍵を1個消費
            boolean consumed = consumeItem(username, DREAM_KEY_ITEM_ID, 1);
            
            if (!consumed) {
                response.put("success", false);
                response.put("message", "アイテムの消費に失敗しました");
                return ResponseEntity.ok(response);
            }

            // 背景を解放済みとして保存
            currentUser.addUnlockedBackground(backgroundId);
            userService.save(currentUser);

            // 残りの所持数を取得
            int remainingCount = userService.getUserMaterialCount(username, DREAM_KEY_ITEM_ID);
            
            response.put("success", true);
            response.put("message", "背景を解放しました!");
            response.put("remainingCount", remainingCount);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 背景選択APIエンドポイント
     */
    @PostMapping("/characters/backgrounds/select")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> selectBackground(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 認証チェック
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "ログインが必要です");
                return ResponseEntity.status(401).body(response);
            }

            String username = userDetails.getUsername();
            String backgroundCode = (String) request.get("backgroundCode");

            // ユーザー情報取得
            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "ユーザーが見つかりません");
                return ResponseEntity.status(404).body(response);
            }

            // 背景を選択状態として保存
            currentUser.setSelectedBackground(backgroundCode);
            userService.save(currentUser);
            
            response.put("success", true);
            response.put("message", "背景を選択しました!");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * アイテムを消費するメソッド(1レコード=1個方式)
     */
    private boolean consumeItem(String username, Long itemId, int amount) {
        try {
            // 該当するアイテムを全て取得
            var items = userItemRepository.findAllByUser_UsernameAndItemId(username, itemId);
            
            // 所持数チェック
            if (items.size() < amount) {
                return false;
            }

            // 指定された個数分だけレコードを削除
            for (int i = 0; i < amount; i++) {
                userItemRepository.delete(items.get(i));
            }

            return true;
        } catch (Exception e) {
            System.err.println("アイテム消費エラー: " + e.getMessage());
            return false;
        }
    }
}