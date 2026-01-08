package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gacha_results")
@Data
@NoArgsConstructor
public class GachaResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;       // ユーザーID
    private String itemName;   // アイテム名
    private String rarity;     // レアリティ
    
    // ★修正: GachaServiceに合わせてフィールド名を変更
    private String drawDateTime; 

    // コンストラクタ
    public GachaResult(Long userId, String itemName, String rarity, String drawDateTime) {
        this.userId = userId;
        this.itemName = itemName;
        this.rarity = rarity;
        this.drawDateTime = drawDateTime;
    }

    // --- Getter / Setter (Lombok @Data で自動生成されますが、明示的に書く場合は以下) ---
    // ※ @Data があるので本来は不要ですが、念のため古いコードとの互換性で残す場合は以下のように名前を修正してください

    /*
    public String getDrawDateTime() {
        return drawDateTime;
    }

    public void setDrawDateTime(String drawDateTime) {
        this.drawDateTime = drawDateTime;
    }
    */
}