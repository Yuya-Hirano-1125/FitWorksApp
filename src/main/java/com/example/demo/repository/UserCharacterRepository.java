package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// ★修正点: model ではなく entity パッケージのクラスをインポートします
import com.example.demo.entity.UserCharacter;

@Repository
public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {

    // 特定のユーザーが持っているキャラクターリストを取得する
    List<UserCharacter> findByUserId(Long userId);
    
    // 既に持っているかチェックする
    boolean existsByUserIdAndCharacterId(Long userId, Long characterId);
}