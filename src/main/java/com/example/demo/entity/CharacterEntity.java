package com.example.demo.entity;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
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
    private int unlockCost;         // 必要素材数（基本素材）

    @Column(name = "image_path")
    private String imagePath;       // 画像パス

    // ===== 進化素材フィールド =====
    // Map形式で「素材名 → 必要数」を保持
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "character_evolution_materials",
        joinColumns = @JoinColumn(name = "character_id")
    )
    @MapKeyColumn(name = "material_name")
    @Column(name = "material_amount")
    private Map<String, Integer> evolutionMaterials = new HashMap<>();

    // ===== 新規追加: 進化条件フィールド =====
    // Map形式で「条件種類 → 条件内容」を保持
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "character_evolution_conditions",
        joinColumns = @JoinColumn(name = "character_id")
    )
    @MapKeyColumn(name = "condition_type")   // 例: requiredLevel, requiredTitle, unlockedCharacter
    @Column(name = "condition_value")        // 例: "20", "炎の挑戦者", "ドラコ解放済み"
    private Map<String, String> evolutionConditions = new HashMap<>();

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

    public Map<String, Integer> getEvolutionMaterials() {
        return evolutionMaterials;
    }
    public void setEvolutionMaterials(Map<String, Integer> evolutionMaterials) {
        this.evolutionMaterials = evolutionMaterials;
    }

    public Map<String, String> getEvolutionConditions() {
        return evolutionConditions;
    }
    public void setEvolutionConditions(Map<String, String> evolutionConditions) {
        this.evolutionConditions = evolutionConditions;
    }
}
