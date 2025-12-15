package com.example.demo.repository;

import java.util.List;
import java.util.Optional; // Optional をインポート

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.BackgroundItem; 

/**
 * BackgroundItem エンティティのデータベース操作を担当するリポジトリインターフェース。
 * JpaRepositoryを継承することで、基本的なCRUD操作（検索、保存、更新、削除）が自動的に提供されます。
 */
@Repository
public interface BackgroundItemRepository extends JpaRepository<BackgroundItem, Long> {
    
    /**
     * 【必要な機能 1】すべての背景アイテムをデータベースから参照し、リストとして取得する。
     * * @return すべての BackgroundItem エンティティのリスト
     */
    List<BackgroundItem> findAll();
    
    /**
     * 【追加機能 2: IDによる検索】背景アイテムのID（主キー）を基に、単一のアイテムを取得する。
     * ※ JpaRepository に既に存在するメソッドですが、型指定のために含めます。
     *
     * @param id BackgroundItem の主キー（ID）
     * @return 存在すれば Optional に包まれた BackgroundItem、存在しなければ Optional.empty()
     */
    Optional<BackgroundItem> findById(Long id);
    
    /**
     * 【修正済み・機能3】解放レベルが特定のレベル以下の背景アイテムのみを取得する。
     * ※ 以前のログに基づき、プロパティ名のエラーを解消するため 'UserLevel' を 'userlevel' に修正。
     * * @param currentLevel ユーザーの現在のレベル
     * @return 必要なレベル（userlevel）以下の BackgroundItem エンティティのリスト
     */
    // ⬇️ 修正箇所: 'UserLevel' を 'userlevel' に変更
    List<BackgroundItem> findByUserLevelLessThanEqual(int userLevel);
}