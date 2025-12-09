package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.ItemCountDTO;
import com.example.demo.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // --- アイテムタイプで検索（登録順で並べる） ---
    @Query("SELECT i FROM Item i WHERE i.type = :type ORDER BY i.sortOrder ASC")
    List<Item> findByType(@Param("type") String type);

    // --- ユーザーごとの所持数を集計（登録順で並べる） ---
    // LEFT JOIN を使うことで、ユーザーがまだ持っていないアイテムも count=0 で返す
    @Query("SELECT new com.example.demo.dto.ItemCountDTO(" +
           "i.name, i.imagePath, " +
           "COALESCE(SUM(CASE WHEN ui.id IS NOT NULL THEN 1 ELSE 0 END), 0), " +
           "i.rarity) " +
           "FROM Item i " +
           "LEFT JOIN UserItem ui ON i.id = ui.item.id AND ui.user.id = :userId " +
           "GROUP BY i.id, i.name, i.imagePath, i.rarity, i.sortOrder " +
           "ORDER BY i.sortOrder ASC")
    List<ItemCountDTO> findItemCountsByUserId(@Param("userId") Long userId);

    // --- 全アイテムを登録順で取得するメソッド ---
    @Query("SELECT i FROM Item i ORDER BY i.sortOrder ASC")
    List<Item> findAllOrderBySortOrder();
}
