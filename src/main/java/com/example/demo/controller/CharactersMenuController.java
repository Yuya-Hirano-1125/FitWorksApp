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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.BackgroundItem;
import com.example.demo.entity.CharacterEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.BackgroundItemRepository;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.repository.UserItemRepository;
import com.example.demo.service.UserService;

@Controller
public class CharactersMenuController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserItemRepository userItemRepository;

    @Autowired
    private CharacterRepository characterRepository;
    
    @Autowired
    private BackgroundItemRepository backgroundItemRepository;

    private static final Long DREAM_KEY_ITEM_ID = 16L;

    private final Map<Long, String> detailMap = new HashMap<>() {{
        put(0L, "dracoegg");
        put(10L, "draco");
        put(20L, "dracos");
        put(30L, "dragonoid");
        put(40L, "dollyegg");
        put(50L, "dolly");
        put(60L, "dolphy");
        put(70L, "dolphinas");
        put(80L, "shiruegg");
        put(90L, "shiru");
        put(100L, "shirufa");
        put(110L, "shirufina");
        put(120L, "merryegg");
        put(130L, "merry");
        put(140L, "meriru");
        put(150L, "merinoa");
        put(160L, "robiegg");
        put(170L, "robi");
        put(180L, "robus");
        put(190L, "robius");
    }};

    @GetMapping("/characters/menu/CharactersMenu")
    public String showCharactersMenu() {
        return "characters/menu/CharactersMenu"; 
    }

    // ★削除: showCharactersUnlock メソッドを削除しました
    // (CharactersUnlockViewController側を使用するため)

    @GetMapping("/characters/menu/CharactersStorage")
    public String showCharactersStorage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findByUsername(userDetails.getUsername());
        List<CharacterEntity> allCharacters = characterRepository.findAll(); 
        List<Map<String, Object>> displayList = new ArrayList<>();
        Long selectedId = user.getSelectedCharacterId();
        if (selectedId == null) selectedId = 0L;

        for (CharacterEntity chara : allCharacters) {
            boolean isUnlocked = user.hasUnlockedCharacter(chara.getId()) || (chara.getId() == 0); 
            Map<String, Object> map = new HashMap<>();
            map.put("id", chara.getId());
            map.put("name", chara.getName());
            map.put("attribute", chara.getAttribute());
            map.put("imgUrl", chara.getImagePath());
            map.put("requiredLevel", chara.getRequiredLevel());
            map.put("rarity", chara.getRarity());
            map.put("isUnlocked", isUnlocked); 
            map.put("isSelected", selectedId.equals(chara.getId()));
            map.put("filename", detailMap.get(chara.getId()));
            displayList.add(map);
        }
        
        long totalCharacters = displayList.size();
        long ownedCharacters = displayList.stream().filter(m -> (Boolean) m.get("isUnlocked")).count();

        model.addAttribute("totalCharacters", totalCharacters);
        model.addAttribute("ownedCharacters", ownedCharacters);
        model.addAttribute("charaList", displayList);

        return "characters/menu/CharactersStorage"; 
    }

    @GetMapping("/characters/detail/{name}")
    public String showCharacterDetail(@PathVariable String name) {
        return "characters/characterdetail/" + name;
    }

    @PostMapping("/characters/select")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> selectCharacter(@RequestBody Map<String, Long> request, @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "ログインが必要です");
            return ResponseEntity.status(401).body(response);
        }
        try {
            Long characterId = request.get("characterId");
            User user = userService.findByUsername(userDetails.getUsername());
            if (characterId != 0 && !user.hasUnlockedCharacter(characterId)) {
                response.put("success", false);
                response.put("message", "このキャラクターはまだ解放されていません。");
                return ResponseEntity.ok(response);
            }
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

    @GetMapping("/characters/menu/Backgrounds")
    public String showBackgrounds(
            @AuthenticationPrincipal UserDetails userDetails, 
            @RequestParam(value = "from", required = false) String from,
            Model model) {
        
        int userLevel = 1;
        int dreamKeyCount = 0;
        Set<String> unlockedBackgrounds = new HashSet<>();
        String selectedBackground = null; 
        
        if (userDetails != null) {
            String username = userDetails.getUsername();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser != null) {
                userLevel = currentUser.getLevel();
                dreamKeyCount = userService.getUserMaterialCount(username, DREAM_KEY_ITEM_ID);
                unlockedBackgrounds = currentUser.getUnlockedBackgrounds();
                selectedBackground = currentUser.getSelectedBackground();
            }
        }
        
        // ★データベースから背景リストを取得して追加
        List<BackgroundItem> allBackgrounds = backgroundItemRepository.findAll();
        model.addAttribute("backgroundList", allBackgrounds);

        model.addAttribute("userLevel", userLevel);
        model.addAttribute("dreamKeyCount", dreamKeyCount);
        model.addAttribute("unlockedBackgrounds", unlockedBackgrounds);
        model.addAttribute("selectedBackground", selectedBackground);
        
        model.addAttribute("from", from);
        
        return "characters/menu/Backgrounds"; 
    }

    @PostMapping("/characters/backgrounds/unlock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unlockBackground(@RequestBody Map<String, Object> request, @AuthenticationPrincipal UserDetails userDetails) {
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

            if (currentUser.hasUnlockedBackground(backgroundId)) {
                response.put("success", false);
                response.put("message", "この背景は既に解放されています");
                return ResponseEntity.ok(response);
            }

            boolean isFreeUnlock = "fire".equals(backgroundId);

            if (!isFreeUnlock) {
                int dreamKeyCount = userService.getUserMaterialCount(username, DREAM_KEY_ITEM_ID);
                if (dreamKeyCount <= 0) {
                    response.put("success", false);
                    response.put("message", "夢幻の鍵が足りません");
                    return ResponseEntity.ok(response);
                }
                boolean consumed = consumeItem(username, DREAM_KEY_ITEM_ID, 1);
                if (!consumed) {
                    response.put("success", false);
                    response.put("message", "アイテムの消費に失敗しました");
                    return ResponseEntity.ok(response);
                }
            }

            currentUser.addUnlockedBackground(backgroundId);
            userService.save(currentUser);

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

    @PostMapping("/characters/backgrounds/select")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> selectBackground(@RequestBody Map<String, Object> request, @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "ログインが必要です");
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

    private boolean consumeItem(String username, Long itemId, int amount) {
        try {
            var items = userItemRepository.findAllByUser_UsernameAndItemId(username, itemId);
            if (items.size() < amount) return false;
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