package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // --- アイテムタイプで検索（登録順で並べる） ---
    @Query("SELECT i FROM Item i WHERE i.type = :type ORDER BY i.sortOrder ASC")
    List<Item> findByType(@Param("type") String type);

    // ★削除: findItemCountsByUserId (UserItemエンティティ廃止のため)
    // 今後はController/Service側で findAllOrderBySortOrder と User.inventory を組み合わせて計算します。

    // --- 全アイテムを sortOrder 順で取得 ---
    @Query("SELECT i FROM Item i ORDER BY i.sortOrder ASC")
    List<Item> findAllOrderBySortOrder();

    // --- アイテム名で検索 ---
    Item findByName(String name);
}