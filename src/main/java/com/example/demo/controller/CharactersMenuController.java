package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

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

import com.example.demo.entity.User;
import com.example.demo.repository.UserItemRepository;
import com.example.demo.service.UserService;

@Controller
public class CharactersMenuController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserItemRepository userItemRepository;

    // 夢幻の鍵のアイテムID
    private static final Long DREAM_KEY_ITEM_ID = 16L;

    // ホームのキャラクターボタンからキャラクターメニューへ遷移
    @GetMapping("/characters/menu/CharactersMenu")
    public String showCharactersMenu() {
        return "characters/menu/CharactersMenu"; 
    }

    // キャラクター一覧画面へ遷移
    @GetMapping("/characters/menu/CharactersStorage")
    public String showCharactersStorage() {
        return "characters/menu/CharactersStorage"; 
    }

    // キャラクター解放画面へ遷移
    @GetMapping("/characters/menu/CharactersUnlock")
    public String showCharactersUnlock() {
        return "characters/menu/CharactersUnlock"; 
    }

    // 背景一覧画面へ遷移
    @GetMapping("/characters/menu/Backgrounds")
    public String showBackgrounds(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        int userLevel = 1;
        int dreamKeyCount = 0;
        
        if (userDetails != null) {
            String username = userDetails.getUsername();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser != null) {
                userLevel = currentUser.getLevel();
                // 夢幻の鍵の所持数を取得
                dreamKeyCount = userService.getUserMaterialCount(username, DREAM_KEY_ITEM_ID);
            }
        }
        
        model.addAttribute("userLevel", userLevel);
        model.addAttribute("dreamKeyCount", dreamKeyCount);
        
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
            // 認証チェック
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "ログインが必要です");
                return ResponseEntity.status(401).body(response);
            }

            String username = userDetails.getUsername();
            String backgroundId = (String) request.get("backgroundId");
            Integer materialId = (Integer) request.get("materialId");

            System.out.println("==========================================");
            System.out.println("DEBUG: 背景解放リクエスト");
            System.out.println("==========================================");
            System.out.println("ユーザー名: " + username);
            System.out.println("背景ID: " + backgroundId);
            System.out.println("素材ID: " + materialId);
            System.out.println("==========================================");

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

            // 解放成功
            int remainingCount = userService.getUserMaterialCount(username, DREAM_KEY_ITEM_ID);
            
            System.out.println("==========================================");
            System.out.println("DEBUG: 解放成功");
            System.out.println("==========================================");
            System.out.println("残りの夢幻の鍵: " + remainingCount);
            System.out.println("==========================================");

            response.put("success", true);
            response.put("message", "背景を解放しました！");
            response.put("remainingCount", remainingCount);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("エラー: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * アイテムを消費するメソッド（1レコード=1個方式）
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
                System.out.println("削除: UserItem ID = " + items.get(i).getId());
            }

            return true;
        } catch (Exception e) {
            System.err.println("アイテム消費エラー: " + e.getMessage());
            return false;
        }
    }
}