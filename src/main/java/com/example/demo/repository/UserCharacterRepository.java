package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.UserCharacter;

// ↓ ここが重要です！ "extends JpaRepository<UserCharacter, Long>" があることで save() が使えるようになります
@Repository
public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {

    // 特定のユーザーが持っているキャラクターリストを取得する
    List<UserCharacter> findByUserId(Long userId);
    
    // 既に持っているかチェックする (この定義を書くことで使えるようになります)
    boolean existsByUserIdAndCharacterId(Long userId, Long characterId);
}