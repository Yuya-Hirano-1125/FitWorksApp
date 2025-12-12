package com.example.demo.repository;

import java.util.List;
import java.util.Optional; // ★追加

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;
import com.example.demo.entity.UserItem;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    // ----------------------------------------
    // ▼ User エンティティで検索（既存ユーザー向け）
    // ----------------------------------------

    // 特定のユーザーの所持アイテムを全て取得
    List<UserItem> findByUser(User user);

    // ユーザーが特定の item を持っているか確認
    boolean existsByUserAndItemId(User user, Long itemId);



    // ----------------------------------------
    // ▼ userId / itemId を指定して検索（より軽量）
    // ----------------------------------------

    // userId で所持アイテム一覧を取得
    List<UserItem> findByUserId(Long userId);

    // userId と itemId で所持確認
    boolean existsByUserIdAndItemId(Long userId, Long itemId);
    
 // ユーザーIDとアイテムIDで、具体的な所持データ（個数など）を取得します
    Optional<UserItem> findByUserIdAndItemId(Long userId, Long itemId);
}
