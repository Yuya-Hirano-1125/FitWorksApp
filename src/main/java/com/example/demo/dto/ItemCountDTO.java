package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemCountDTO {
    private String name;       // アイテム名
    private String imagePath;  // アイテム画像のパス
    private Long count;        // 所持数
}
