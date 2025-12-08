package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.ItemCountDTO;
import com.example.demo.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // --- 既存メソッド（残す） ---
    
    // アイテムタイプで検索
    List<Item> findByType(String type);

    // --- 修正版メソッド（ユーザーごとの所持数を集計） ---
    // LEFT JOIN を使うことで、ユーザーがまだ持っていないアイテムも count=0 で返す
    @Query("SELECT new com.example.demo.dto.ItemCountDTO(i.name, i.imagePath, COALESCE(COUNT(ui.id), 0)) " +
           "FROM Item i LEFT JOIN UserItem ui ON i.id = ui.item.id AND ui.user.id = :userId " +
           "GROUP BY i.id, i.name, i.imagePath")
    List<ItemCountDTO> findItemCountsByUserId(@Param("userId") Long userId);
}
