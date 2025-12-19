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
    // ▼ User エンティティで検索
    // ----------------------------------------

    List<UserItem> findByUser(User user);

    boolean existsByUserAndItemId(User user, Long itemId);

    Optional<UserItem> findByUser_UsernameAndItem_Type(String username, String type);

    // ★ このメソッドでリストを取得し、Service側でquantityを合計する
    List<UserItem> findAllByUser_UsernameAndItemId(String username, Long itemId);

    // ----------------------------------------
    // ▼ userId / itemId を指定して検索
    // ----------------------------------------

    List<UserItem> findByUserId(Long userId);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    List<UserItem> findAllByUserIdAndItemId(Long userId, Long itemId);
    
}