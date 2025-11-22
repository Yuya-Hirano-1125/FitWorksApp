package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "items")
@Data
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // アイテム名 (例: 森の背景, 赤い帽子)

    @Column(nullable = false)
    private String type; // アイテムの種別 (例: BACKGROUND, COSTUME)

    @Column(nullable = false)
    private String imagePath; // 表示用ファイルパス (src/main/resources/static/img/costume/...)
    
    private String description;
}