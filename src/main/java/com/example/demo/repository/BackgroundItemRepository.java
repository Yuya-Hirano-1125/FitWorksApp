package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.BackgroundItem; 

/**
 * BackgroundItem エンティティのデータベース操作を担当するリポジトリインターフェース。
 * JpaRepositoryを継承することで、基本的なCRUD操作(検索、保存、更新、削除)が自動的に提供されます。
 */
@Repository
public interface BackgroundItemRepository extends JpaRepository<BackgroundItem, Long> {
    
    /**
     * 【機能1】すべての背景アイテムをデータベースから参照し、リストとして取得する。
     * @return すべての BackgroundItem エンティティのリスト
     */
    List<BackgroundItem> findAll();
    
    /**
     * 【機能2】IDによる検索: 背景アイテムのID(主キー)を基に、単一のアイテムを取得する。
     * ※ JpaRepository に既に存在するメソッドですが、型指定のために明示的に記載
     *
     * @param id BackgroundItem の主キー(ID)
     * @return 存在すれば Optional に包まれた BackgroundItem、存在しなければ Optional.empty()
     */
    Optional<BackgroundItem> findById(Long id);
    
    /**
     * 【機能3】解放レベルが特定のレベル以下の背景アイテムのみを取得する。
     * 
     * @param userLevel ユーザーの現在のレベル
     * @return 必要なレベル(user_level)以下の BackgroundItem エンティティのリスト
     */
    List<BackgroundItem> findByUserLevelLessThanEqual(int userLevel);
    
    /**
     * 【NEW - 機能4】背景名で検索: 背景名を基に背景アイテムを取得する。
     * 
     * @param bgname 背景名
     * @return 該当する BackgroundItem、存在しなければ Optional.empty()
     */
    Optional<BackgroundItem> findByBgname(String bgname);
    
    /**
     * 【NEW - 機能5】装備中の背景アイテムIDで検索: 特定の装備IDを持つ背景アイテムを取得する。
     * 
     * @param equippedBackgroundItemId 装備中の背景アイテムID
     * @return 該当する BackgroundItem のリスト
     */
    List<BackgroundItem> findByEquippedBackgroundItemId(String equippedBackgroundItemId);
    
    /**
     * 【NEW - 機能6】素材の有無で検索: 素材が必要な背景アイテム、または不要な背景アイテムを取得する。
     * 
     * @param hasMaterial true=素材が必要、false=素材不要
     * @return 該当する BackgroundItem のリスト
     */
    List<BackgroundItem> findByHasMaterial(Boolean hasMaterial);
    
    /**
     * 【NEW - 機能7】複合条件検索: ユーザーレベル以下で、素材が不要な背景アイテムを取得する。
     * レベルで解放可能な背景アイテムを取得する際に便利です。
     * 
     * @param userLevel ユーザーの現在のレベル
     * @param hasMaterial 素材の有無(通常はfalseを指定)
     * @return 条件に一致する BackgroundItem のリスト
     */
    List<BackgroundItem> findByUserLevelLessThanEqualAndHasMaterial(int userLevel, Boolean hasMaterial);
    
    /**
     * 【NEW - 機能8】カスタムクエリ: IDのリストで複数の背景アイテムを一括取得する。
     * 複数の背景IDを指定して一度に取得したい場合に使用します。
     * 
     * @param ids 背景アイテムのIDリスト
     * @return 該当する BackgroundItem のリスト
     */
    @Query("SELECT b FROM BackgroundItem b WHERE b.id IN :ids")
    List<BackgroundItem> findByIdIn(@Param("ids") List<Long> ids);
    
    /**
     * 【NEW - 機能9】カスタムクエリ: 背景名の部分一致検索(大文字小文字を区別しない)
     * 
     * @param keyword 検索キーワード
     * @return 背景名にキーワードを含む BackgroundItem のリスト
     */
    @Query("SELECT b FROM BackgroundItem b WHERE LOWER(b.bgname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BackgroundItem> searchByBgnameContaining(@Param("keyword") String keyword);
    
    /**
     * 【NEW - 機能10】レベル範囲で検索: 特定のレベル範囲内の背景アイテムを取得する。
     * 
     * @param minLevel 最小レベル
     * @param maxLevel 最大レベル
     * @return レベル範囲内の BackgroundItem のリスト
     */
    List<BackgroundItem> findByUserLevelBetween(int minLevel, int maxLevel);
}