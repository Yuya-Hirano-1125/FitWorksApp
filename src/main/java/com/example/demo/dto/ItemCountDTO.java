package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // ★追加

@Data
@AllArgsConstructor
@NoArgsConstructor // ★追加: 引数なしコンストラクタを生成
public class ItemCountDTO {
    private String name;       // アイテム名
    private String imagePath;  // アイテム画像のパス
    private Integer count;     // ★変更: 所持数をLongからIntegerに変更 (Map<Item, Integer>に合わせる)
    private String rarity;     // レア度 (R, SR, SSR, UR)
}