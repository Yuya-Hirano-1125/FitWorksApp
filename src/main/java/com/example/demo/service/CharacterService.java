package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.CharacterUnlockStatus;
import com.example.demo.model.UserCharacter;
import com.example.demo.repository.UserCharacterRepository;

@Service
public class CharacterService {

    @Autowired
    private UserCharacterRepository userCharacterRepository;

    /**
     * 【進化画面用】
     * ユーザーが既に解放しているキャラクターIDのセットを取得する。
     */
    public Set<Long> getUnlockedCharacterIds(Long userId) {
        List<UserCharacter> unlockedData = userCharacterRepository.findByUserId(userId);
        return unlockedData.stream()
                .map(UserCharacter::getCharacterId)
                .collect(Collectors.toSet());
    }

    /**
     * 【保管画面用】
     * 全キャラクターの定義リストを作成し、ユーザーの所持状況（isUnlocked）を反映して返す。
     */
    public List<CharacterUnlockStatus> getCharacterUnlockStatus(Long userId) {
        
        // 1. DBから解放済みIDを取得
        Set<Long> unlockedIds = getUnlockedCharacterIds(userId);

        // 2. 全キャラクターのマスタデータ定義
        List<CharacterUnlockStatus> allCharacters = new ArrayList<>();

        // --- 🔥 炎属性 (0, 10, 20, 30) ---
        allCharacters.add(new CharacterUnlockStatus(0L, "エンバーハート", "fire", 1, true, "/img/character/0.png", "★1"));
        allCharacters.add(new CharacterUnlockStatus(10L, "ドラコ", "fire", 10, false, "/img/character/10.png", "★2"));
        allCharacters.add(new CharacterUnlockStatus(20L, "ドラコス", "fire", 20, false, "/img/character/20.png", "★3"));
        allCharacters.add(new CharacterUnlockStatus(30L, "ドラグノイド", "fire", 30, false, "/img/character/30.png", "★4"));

        // --- 💧 水属性 (40, 50, 60, 70) ---
        allCharacters.add(new CharacterUnlockStatus(40L, "ルーナドロップ", "water", 40, false, "/img/character/40.png", "★1"));
        allCharacters.add(new CharacterUnlockStatus(50L, "ドリー", "water", 50, false, "/img/character/50.png", "★2"));
        allCharacters.add(new CharacterUnlockStatus(60L, "ドルフィ", "water", 60, false, "/img/character/60.png", "★3"));
        allCharacters.add(new CharacterUnlockStatus(70L, "ドルフィナス", "water", 70, false, "/img/character/70.png", "★4"));

        // --- 🌿 草属性 (80, 90, 100, 110) ---
        allCharacters.add(new CharacterUnlockStatus(80L, "フォリアン", "grass", 80, false, "/img/character/80.png", "★1"));
        allCharacters.add(new CharacterUnlockStatus(90L, "シル", "grass", 90, false, "/img/character/90.png", "★2"));
        allCharacters.add(new CharacterUnlockStatus(100L, "シルファ", "grass", 100, false, "/img/character/100.png", "★3"));
        allCharacters.add(new CharacterUnlockStatus(110L, "シルフィナ", "grass", 110, false, "/img/character/110.png", "★4"));

        // --- ☀️ 光属性 (120, 130, 140, 150) ---
        allCharacters.add(new CharacterUnlockStatus(120L, "ハローネスト", "light", 120, false, "/img/character/120.png", "★1"));
        allCharacters.add(new CharacterUnlockStatus(130L, "メリー", "light", 130, false, "/img/character/130.png", "★2"));
        allCharacters.add(new CharacterUnlockStatus(140L, "メリル", "light", 140, false, "/img/character/140.png", "★3"));
        allCharacters.add(new CharacterUnlockStatus(150L, "メリノア", "light", 150, false, "/img/character/150.png", "★4"));

        // --- 🌙 闇属性 (160, 170, 180, 190) ---
        allCharacters.add(new CharacterUnlockStatus(160L, "ネビュリス", "dark", 160, false, "/img/character/160.png", "★1"));
        allCharacters.add(new CharacterUnlockStatus(170L, "ロービ", "dark", 170, false, "/img/character/170.png", "★2"));
        allCharacters.add(new CharacterUnlockStatus(180L, "ローバス", "dark", 180, false, "/img/character/180.png", "★3"));
        allCharacters.add(new CharacterUnlockStatus(190L, "ロービアス", "dark", 190, false, "/img/character/190.png", "★4"));
        
        // --- ❓ シークレット (250) ---
        allCharacters.add(new CharacterUnlockStatus(250L, "シークレット", "dark", 250, false, "/img/placeholder_final.png", "???"));

        // 3. マッチング処理
        for (CharacterUnlockStatus chara : allCharacters) {
            if (unlockedIds.contains(chara.getId())) {
                chara.setIsUnlocked(true);
            }
        }
        
        return allCharacters;
    }

    /**
     * キャラクターを進化(解放)してDBに保存する。
     */
    @Transactional
    public void unlockCharacter(Long userId, Long characterId, Integer cost) {
        boolean exists = userCharacterRepository.existsByUserIdAndCharacterId(userId, characterId);
        
        if (!exists) {
            UserCharacter newUnlock = new UserCharacter(userId, characterId);
            userCharacterRepository.save(newUnlock);
        }
    }
}