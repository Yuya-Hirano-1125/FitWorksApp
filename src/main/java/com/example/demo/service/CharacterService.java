package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// エンティティ (DB定義)
import com.example.demo.entity.User;
import com.example.demo.entity.UserCharacter;
import com.example.demo.entity.UserItem;
// 画面表示用モデル (DTO)
import com.example.demo.model.CharacterUnlockStatus;
// リポジトリ
import com.example.demo.repository.UserCharacterRepository;
import com.example.demo.repository.UserItemRepository;
import com.example.demo.repository.UserRepository;

@Service
public class CharacterService {

    @Autowired
    private UserCharacterRepository userCharacterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserItemRepository userItemRepository;

    /**
     * ユーザーが既に解放しているキャラクターIDのセットを取得
     */
    public Set<Long> getUnlockedCharacterIds(Long userId) {
        // UserCharacterエンティティからIDだけを抽出してSetにする
        List<UserCharacter> unlockedData = userCharacterRepository.findByUserId(userId);
        return unlockedData.stream()
                .map(UserCharacter::getCharacterId)
                .collect(Collectors.toSet());
    }

    /**
     * 全キャラの定義と所持状態のマージリストを取得
     * (Unlock画面やStorage画面で使用)
     */
    public List<CharacterUnlockStatus> getCharacterUnlockStatus(Long userId) {
        
        Set<Long> unlockedIds = getUnlockedCharacterIds(userId);
        List<CharacterUnlockStatus> allCharacters = new ArrayList<>();

        // --- キャラクター定義 (本来はDBマスタ推奨) ---
        
        // --- 🔥 Fire ---
        allCharacters.add(new CharacterUnlockStatus(0L, "エンバーハート", "fire", 1, true, "/img/character/0.png", "★1"));
        allCharacters.add(new CharacterUnlockStatus(10L, "ドラコ", "fire", 10, false, "/img/character/10.png", "★2"));
        allCharacters.add(new CharacterUnlockStatus(20L, "ドラコス", "fire", 20, false, "/img/character/20.png", "★3"));
        allCharacters.add(new CharacterUnlockStatus(30L, "ドラグノイド", "fire", 30, false, "/img/character/30.png", "★4"));

        // --- 💧 Water ---
        allCharacters.add(new CharacterUnlockStatus(40L, "ルーナドロップ", "water", 40, false, "/img/character/40.png", "★1"));
        allCharacters.add(new CharacterUnlockStatus(50L, "ドリー", "water", 50, false, "/img/character/50.png", "★2"));
        allCharacters.add(new CharacterUnlockStatus(60L, "ドルフィ", "water", 60, false, "/img/character/60.png", "★3"));
        allCharacters.add(new CharacterUnlockStatus(70L, "ドルフィナス", "water", 70, false, "/img/character/70.png", "★4"));

        // --- 🌿 Grass ---
        allCharacters.add(new CharacterUnlockStatus(80L, "フォリアン", "grass", 80, false, "/img/character/80.png", "★1"));
        allCharacters.add(new CharacterUnlockStatus(90L, "シル", "grass", 90, false, "/img/character/90.png", "★2"));
        allCharacters.add(new CharacterUnlockStatus(100L, "シルファ", "grass", 100, false, "/img/character/100.png", "★3"));
        allCharacters.add(new CharacterUnlockStatus(110L, "シルフィナ", "grass", 110, false, "/img/character/110.png", "★4"));

        // --- ✨ Light ---
        allCharacters.add(new CharacterUnlockStatus(120L, "ハローネスト", "light", 120, false, "/img/character/120.png", "★1"));
        allCharacters.add(new CharacterUnlockStatus(130L, "メリー", "light", 130, false, "/img/character/130.png", "★2"));
        allCharacters.add(new CharacterUnlockStatus(140L, "メリル", "light", 140, false, "/img/character/140.png", "★3"));
        allCharacters.add(new CharacterUnlockStatus(150L, "メリノア", "light", 150, false, "/img/character/150.png", "★4"));

        // --- 🌙 Dark ---
        allCharacters.add(new CharacterUnlockStatus(160L, "ネビュリス", "dark", 160, false, "/img/character/160.png", "★1"));
        allCharacters.add(new CharacterUnlockStatus(170L, "ロービ", "dark", 170, false, "/img/character/170.png", "★2"));
        allCharacters.add(new CharacterUnlockStatus(180L, "ローバス", "dark", 180, false, "/img/character/180.png", "★3"));
        allCharacters.add(new CharacterUnlockStatus(190L, "ロービアス", "dark", 190, false, "/img/character/190.png", "★4"));

        // --- ❓ Secret ---
        allCharacters.add(new CharacterUnlockStatus(250L, "シークレット", "dark", 250, false, "/img/placeholder_final.png", "???"));

        // 所持チェック
        for (CharacterUnlockStatus chara : allCharacters) {
            if (unlockedIds.contains(chara.getId())) {
                chara.setIsUnlocked(true);
            }
        }
        return allCharacters;
    }

    /**
     * キャラクター解放処理 (Transactionalで一貫性を保証)
     * レベルチェック、素材消費、キャラ付与を行います。
     */
    @Transactional
    public void unlockCharacter(Long userId, Long characterId, Integer cost) throws Exception {
        
        // 1. 既に所持しているかチェック
        if (userCharacterRepository.existsByUserIdAndCharacterId(userId, characterId)) {
            // 既に持っている場合は何もせず終了（あるいはエラーメッセージを出しても良い）
            return;
        }

        // 2. 解放対象のキャラ情報を特定（必要レベルや属性を知るため）
        CharacterUnlockStatus targetChara = getCharacterUnlockStatus(userId).stream()
                .filter(c -> c.getId().equals(characterId))
                .findFirst()
                .orElseThrow(() -> new Exception("キャラクター定義が見つかりません。"));

        // 3. ユーザー情報の取得（★重要：後で保存時にこのuserオブジェクトを使います）
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("ユーザーが見つかりません。"));
        
        // 4. レベルチェック
        if (user.getLevel() < targetChara.getRequiredLevel()) {
            throw new Exception("レベルが足りません。必要Lv: " + targetChara.getRequiredLevel());
        }

        // 5. 消費アイテムIDの特定（属性に応じて判定）
        Long requiredItemId = getItemIdByAttribute(targetChara.getAttribute());

        // 6. 素材所持数のチェック
        // UserItemRepositoryに追加した findByUserIdAndItemId を使用
        UserItem userItem = userItemRepository.findByUserIdAndItemId(userId, requiredItemId)
                .orElseThrow(() -> new Exception("進化素材を一つも所持していません。"));
        
        if (userItem.getQuantity() < cost) {
            throw new Exception("素材の数が足りません。所持数: " + userItem.getQuantity() + " / 必要数: " + cost);
        }

        // 7. 素材消費 (減算して保存)
        userItem.setQuantity(userItem.getQuantity() - cost);
        userItemRepository.save(userItem);

        // 8. キャラクター付与 (Storageへ保存)
        // ★修正点: UserCharacterのコンストラクタには (Userエンティティ, キャラID) を渡します
        UserCharacter newUnlock = new UserCharacter(user, characterId);
        userCharacterRepository.save(newUnlock);
    }

    /**
     * 属性文字列からアイテムIDを返すヘルパー
     */
    private Long getItemIdByAttribute(String attribute) {
        if (attribute == null) return 1L;
        switch (attribute.toLowerCase()) {
            case "fire": return 1L;  // 紅玉
            case "water": return 2L; // 蒼玉
            case "grass": return 3L; // 翠玉
            case "light": return 4L; // 聖玉
            case "dark": return 5L;  // 闇玉
            default: return 1L;
        }
    }
}