package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;
import com.example.demo.entity.UserItem;


@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    // ----------------------------------------
    // ▼ User エンティティで検索（既存ユーザー向け）
    // ----------------------------------------

    // 特定のユーザーの所持アイテムを全て取得
    List<UserItem> findByUser(User user);

    // ユーザーが特定の item を持っているか確認
    boolean existsByUserAndItemId(User user, Long itemId);

    // ユーザー名とアイテムのタイプで所持数を取得（素材管理用）
    Optional<UserItem> findByUser_UsernameAndItem_Type(String username, String type);

    // ★★★ 変更: 複数レコードを返すメソッド（1レコード=1個方式） ★★★
    List<UserItem> findAllByUser_UsernameAndItemId(String username, Long itemId);

    // ----------------------------------------
    // ▼ userId / itemId を指定して検索（より軽量）
    // ----------------------------------------

    // userId で所持アイテム一覧を取得
    List<UserItem> findByUserId(Long userId);

    // userId と itemId で所持確認
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    // ★★★ 変更: 複数レコードを返す（1レコード=1個方式） ★★★
    List<UserItem> findAllByUserIdAndItemId(Long userId, Long itemId);
}