package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;
import com.example.demo.entity.UserItem;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    
    // --- 既存メソッド（残す） ---
    
    // 特定のユーザーの所有アイテムをすべて取得
    List<UserItem> findByUser(User user);

    // 特定のユーザーが特定のアイテムを所有しているか確認
    boolean existsByUserAndItemId(User user, Long itemId);

    // --- 追加メソッド（userIdを直接指定できるようにする） ---
    
    // userIdで所有アイテム一覧を取得
    List<UserItem> findByUserId(Long userId);

    // userIdとitemIdで所有確認
    boolean existsByUserIdAndItemId(Long userId, Long itemId);
}
