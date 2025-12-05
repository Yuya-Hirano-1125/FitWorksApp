package com.example.demo.model;

public class GachaResult {

    private int id;
    private int userId;        // INT に変更
    private String itemName;
    private String rarity;
    private String createdAt;

    public GachaResult() {}

    public GachaResult(int userId, String itemName, String rarity, String createdAt) {
        this.userId = userId;
        this.itemName = itemName;
        this.rarity = rarity;
        this.createdAt = createdAt;
    }

    // Getter / Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
