package com.example.demo.model;

public class CharacterUnlockStatus {
    private Long id;
    private String name;
    private String attribute;     // "fire", "water" など
    private int requiredLevel;
    private boolean isUnlocked;   // 解放済みかどうか
    private String imgUrl;
    private String evolutionTag;  // "★1", "★2" など

    // コンストラクタ
    public CharacterUnlockStatus(Long id, String name, String attribute, int requiredLevel, boolean isUnlocked, String imgUrl, String evolutionTag) {
        this.id = id;
        this.name = name;
        this.attribute = attribute;
        this.requiredLevel = requiredLevel;
        this.isUnlocked = isUnlocked;
        this.imgUrl = imgUrl;
        this.evolutionTag = evolutionTag;
    }

    // Getter と Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public boolean getIsUnlocked() { return isUnlocked; }
    public void setIsUnlocked(boolean isUnlocked) { this.isUnlocked = isUnlocked; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    public String getEvolutionTag() { return evolutionTag; }
    public void setEvolutionTag(String evolutionTag) { this.evolutionTag = evolutionTag; }
}