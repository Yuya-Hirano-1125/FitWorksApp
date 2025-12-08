package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "item") // ← テーブル名を "item" に修正
@Data
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // アイテム名 (例: 紅玉, 蒼玉)

    @Column(nullable = false)
    private String type; // アイテムの種別 (例: R, SR, SSR, UR)

    @Column(name = "image_path", nullable = false)
    private String imagePath; // 表示用ファイルパス (例: /img/item/R-red.png)

    private String description; // アイテム説明（任意）
}
