package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;
import com.example.demo.entity.UserItem;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    // 特定のユーザーの所有アイテムをすべて取得
    List<UserItem> findByUser(User user);

    // 特定のユーザーが特定のアイテムを所有しているか確認
    boolean existsByUserAndItemId(User user, Long itemId);
}
