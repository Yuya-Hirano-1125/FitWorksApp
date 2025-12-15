package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.BackgroundItem; // BackgroundItemエンティティのパスに置き換えてください

/**
 * BackgroundItem エンティティのデータベース操作を担当するリポジトリインターフェース。
 * JpaRepositoryを継承することで、基本的なCRUD操作（検索、保存、更新、削除）が自動的に提供されます。
 */
@Repository
public interface BackgroundItemRepository extends JpaRepository<BackgroundItem, Long> {
    
    /**
     * 【必要な機能 1】すべての背景アイテムをデータベースから参照し、リストとして取得する。
     * JpaRepositoryを継承しているため、メソッド名を定義するだけで自動的にSQLが生成されます。
     * * @return すべての BackgroundItem エンティティのリスト
     */
    List<BackgroundItem> findAll();
    
    
    /**
     * 【追加機能の例】解放レベルが特定のレベル以下の背景アイテムのみを取得する。
     * フィールド名（userLevel）に基づき、Spring Data JPAがクエリを自動生成します。
     * * @param currentLevel ユーザーの現在のレベル
     * @return 必要なレベル（userLevel）以下の BackgroundItem エンティティのリスト
     */
    // ⬇️ 修正箇所: 'RequiredLevel' を 'UserLevel' に変更 
    List<BackgroundItem> findByUserLevelLessThanEqual(int currentLevel);
}