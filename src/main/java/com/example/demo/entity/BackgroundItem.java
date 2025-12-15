package com.example.demo.entity; // 🚨 パッケージはご自身の環境に合わせて修正してください

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data; // Lombokを使用する場合
import lombok.NoArgsConstructor; // Lombokを使用する場合

/**
 * ホーム画面の背景アイテムを表すエンティティ。
 * データベースの 'background_items' テーブルに対応します。
 */
@Entity
@Table(name = "background_items")
@Data // Lombok: getter, setter, toString, equals, hashCodeを自動生成
@NoArgsConstructor // Lombok: 引数なしコンストラクタを自動生成
public class BackgroundItem {

    /**
     * 主キー (ID)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 背景アイテムの内部コードID (例: fire, water, classroom)
     */
    private String equipped_background_item_id;

    /**
     * 背景の表示名 (例: 炎の世界, 教室)
     */
    private String bgname;

    /**
     * 背景画像のファイルパス (例: /img/background/fire-original.png)
     */
    private String bgimgurl;

    /**
     * この背景をアンロックするために必要なユーザーレベル
     * 0 の場合は最初から解放済み
     */
    private int userLevel;

    /**
     * この背景のアンロックに特定の素材が必要か否か
     * true の場合、レベルに関係なく素材が必要
     */
    private boolean hasMaterial;

    /**
     * 現在、ユーザーがその背景を所有しているか (所有フラグ)
     * * 🚨 注意: 所有情報は Userエンティティ側で管理する方が一般的です。
     * * ここでは、シンプルにアイテムの基本情報のみを定義します。
     */
    // private boolean isOwned; 

    // 必要に応じて、Lombokを使わずに手動でコンストラクタやgetter/setterを追加しても構いません。
    
    // --- 【Lombokを使用しない場合のGetter/Setterの例】 ---
    /*
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBgCode() { return bgCode; }
    public void setBgCode(String bgCode) { this.bgCode = bgCode; }
    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }
    // ... 他のフィールドについても同様
    */
}