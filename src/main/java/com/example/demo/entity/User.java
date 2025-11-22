package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType; // 新規追加
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn; // 新規追加
import jakarta.persistence.ManyToOne; // 新規追加

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email; 

    private Integer level = 1; 
    private Integer experiencePoints = 0; 

    // ★ 既存: デイリーミッション追跡用
    private LocalDate lastMissionCompletionDate;
    private Boolean isRewardClaimedToday = false; 

    // ★ 新規追加: キャラクター装備アイテム
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipped_background_item_id")
    private Item equippedBackgroundItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipped_costume_item_id")
    private Item equippedCostumeItem;


    public User() {
        if (this.level == null) this.level = 1;
        if (this.experiencePoints == null) this.experiencePoints = 0;
        if (this.isRewardClaimedToday == null) this.isRewardClaimedToday = false;
    }

    // --- Getter / Setter (既存) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getLevel() { 
        return level != null ? level : 1; 
    }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getExperiencePoints() { 
        return experiencePoints != null ? experiencePoints : 0; 
    }
    public void setExperiencePoints(Integer experiencePoints) { this.experiencePoints = experiencePoints; }

    public LocalDate getLastMissionCompletionDate() { return lastMissionCompletionDate; }
    public void setLastMissionCompletionDate(LocalDate lastMissionCompletionDate) { this.lastMissionCompletionDate = lastMissionCompletionDate; }
    
    public Boolean getIsRewardClaimedToday() { return isRewardClaimedToday; }
    public void setIsRewardClaimedToday(Boolean isRewardClaimedToday) { this.isRewardClaimedToday = isRewardClaimedToday; }

    // --- Getter / Setter (新規追加: キャラクター装備アイテム) ---
    public Item getEquippedBackgroundItem() { return equippedBackgroundItem; }
    public void setEquippedBackgroundItem(Item equippedBackgroundItem) { this.equippedBackgroundItem = equippedBackgroundItem; }

    public Item getEquippedCostumeItem() { return equippedCostumeItem; }
    public void setEquippedCostumeItem(Item equippedCostumeItem) { this.equippedCostumeItem = equippedCostumeItem; }
    
    // --- レベルアップ関連 (既存) ---
    public int calculateRequiredXp() {
        int currentLevel = getLevel(); 
        return 1000 + (currentLevel - 1) * 200;
    }

    public void addXp(int xp) {
        int currentXp = getExperiencePoints(); 
        this.experiencePoints = currentXp + xp;
        
        while (this.experiencePoints >= calculateRequiredXp()) {
            this.experiencePoints -= calculateRequiredXp();
            this.level++;
        }
    }

    public int getProgressPercent() {
        int currentXp = getExperiencePoints(); 
        int requiredXp = calculateRequiredXp();
        if (requiredXp == 0) return 0; 
        return (int)(((double) currentXp / requiredXp) * 100);
    }
}

