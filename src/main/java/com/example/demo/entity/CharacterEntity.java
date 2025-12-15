package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "characters")
public class CharacterEntity {

    @Id
    private Long id;  // DBでINSERT時にidを指定しているので自動生成は不要

    @Column(name = "name")
    private String name;            // 名前

    @Column(name = "attribute")
    private String attribute;       // 属性 (fire, water, grass, light, dark, secret)

    @Column(name = "rarity")
    private String rarity;          // レアリティ

    @Column(name = "required_level")
    private int requiredLevel;      // 必要レベル

    @Column(name = "unlock_cost")
    private int unlockCost;         // 必要素材数

    @Column(name = "image_path")    // ← DBのカラム名に合わせて修正
    private String imagePath;       // 画像パス


    // ===== Getter / Setter =====
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAttribute() {
        return attribute;
    }
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getRarity() {
        return rarity;
    }
    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public int getUnlockCost() {
        return unlockCost;
    }
    public void setUnlockCost(int unlockCost) {
        this.unlockCost = unlockCost;
    }

    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
