package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "background_items")
@Data
@NoArgsConstructor
public class BackgroundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 背景ID (例: fire, classroom) ※HTMLのdata-idに対応
    @Column(unique = true)
    private String backgroundId;

    // カテゴリ (nature, special) ※HTMLのdata-categoryに対応
    private String category;

    // 表示名 (例: 炎の世界)
    private String bgname;

    // 画像パス
    private String bgimgurl;
    
    // 背景画像コード (例: fire-original) ※CSSやJSで画像切り替えに使うID
    private String bgCode;

    // 解放レベル (0の場合はレベル制限なし)
    private int userLevel;

    // 素材が必要かどうか
    private boolean hasMaterial;
    
    // 必要素材のID (例: 16)
    private Long requiredMaterialId;
    
    // 必要素材の名前 (例: 夢幻の鍵)
    private String requiredMaterialName;
}