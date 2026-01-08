package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.repository.ItemRepository; // 追加
// import com.example.demo.repository.UserItemRepository; // 削除
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.UserService;

@Controller
public class CharactersMenuController {

    @Autowired
    private UserService userService;

    // 削除: UserItemRepository
    // @Autowired
    // private UserItemRepository userItemRepository;
    
    // 追加: アイテム検索用に必要
    @Autowired
    private ItemRepository itemRepository;

    // メニュー画面表示
    @GetMapping("/characters/menu/CharactersMenu")
    public String showMenu(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("user", user);
            // 選択中の背景コードを渡す
            model.addAttribute("selectedBackground", user.getSelectedBackground());
        }
        return "characters/menu/CharactersMenu";
    }

    // 背景変更API
    @PostMapping("/api/characters/background/change")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changeBackground(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "ログインしてください");
                return ResponseEntity.status(401).body(response);
            }

            String username = userDetails.getUsername();
            String backgroundCode = (String) request.get("backgroundCode");

            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "ユーザーが見つかりません");
                return ResponseEntity.status(404).body(response);
            }

            // 背景変更処理
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
     * 指定したアイテムを消費するヘルパーメソッド (修正版)
     * UserItemRepositoryを使わず、Userエンティティのinventory操作メソッドを使用します。
     */
    private boolean consumeItem(String username, Long itemId, int amount) {
        try {
            // 1. ユーザーを取得
            User user = userService.findByUsername(username);
            if (user == null) return false;

            // 2. アイテムマスタから対象のアイテムを取得
            Item item = itemRepository.findById(itemId).orElse(null);
            if (item == null) return false;

            // 3. Userクラスのメソッドを使って消費を試みる
            // (useItemメソッド内で所持数チェックと減算が行われます)
            boolean success = user.useItem(item, amount);

            if (success) {
                // 4. 変更を保存 (Userエンティティを保存すればinventoryの変更も反映される)
                userService.save(user);
                return true;
            } else {
                // 所持数が足りない場合
                return false;
            }

        } catch (Exception e) {
            System.err.println("アイテム消費エラー: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}