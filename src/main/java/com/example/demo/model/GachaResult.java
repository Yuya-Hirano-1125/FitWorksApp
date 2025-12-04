package com.example.demo.model;

public class GachaResult {

    private int id;           // 自動採番
    private String userId;    // ユーザーID
    private String itemName;  // アイテム名
    private String rarity;    // R / SR / SSR
    private String createdAt; // 保存日時

    public GachaResult() {}

    public GachaResult(String userId, String itemName, String rarity, String createdAt) {
        this.userId = userId;
        this.itemName = itemName;
        this.rarity = rarity;
        this.createdAt = createdAt;
    }

    // ---------- Getter & Setter ----------
    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }

    public String getUserId() { 
        return userId; 
    }
    public void setUserId(String userId) { 
        this.userId = userId; 
    }

    public String getItemName() { 
        return itemName; 
    }
    public void setItemName(String itemName) { 
        this.itemName = itemName; 
    }

    public String getRarity() { 
        return rarity; 
    }
    public void setRarity(String rarity) { 
        this.rarity = rarity; 
    }

    public String getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(String createdAt) { 
        this.createdAt = createdAt; 
    }
}
