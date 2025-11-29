package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ★★★ 経験値(XP)フィールド ★★★
    private int xp = 0; // 初期値は0

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

    // ★★★ 新規追加: 設定項目 ★★★
    // 1. トレーニングリマインダー通知 (デフォルトON)
    private boolean notificationTrainingReminder = true;

    // 2. AIコーチの提案通知 (デフォルトON)
    private boolean notificationAiSuggestion = true;

    // 3. 進捗レポートメール (デフォルトOFF)
    private boolean notificationProgressReport = false;

    // 4. テーマ設定 (デフォルト "default")
    private String theme = "default";

    public User() {
        if (this.level == null) this.level = 1;
        if (this.experiencePoints == null) this.experiencePoints = 0;
        if (this.isRewardClaimedToday == null) this.isRewardClaimedToday = false;
    }

    // --- Getter / Setter ---
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

    public Item getEquippedBackgroundItem() { return equippedBackgroundItem; }
    public void setEquippedBackgroundItem(Item equippedBackgroundItem) { this.equippedBackgroundItem = equippedBackgroundItem; }

    public Item getEquippedCostumeItem() { return equippedCostumeItem; }
    public void setEquippedCostumeItem(Item equippedCostumeItem) { this.equippedCostumeItem = equippedCostumeItem; }

    // ★★★ 追加: 設定項目のGetter/Setter ★★★
    public boolean isNotificationTrainingReminder() { return notificationTrainingReminder; }
    public void setNotificationTrainingReminder(boolean notificationTrainingReminder) { this.notificationTrainingReminder = notificationTrainingReminder; }

    public boolean isNotificationAiSuggestion() { return notificationAiSuggestion; }
    public void setNotificationAiSuggestion(boolean notificationAiSuggestion) { this.notificationAiSuggestion = notificationAiSuggestion; }

    public boolean isNotificationProgressReport() { return notificationProgressReport; }
    public void setNotificationProgressReport(boolean notificationProgressReport) { this.notificationProgressReport = notificationProgressReport; }

    public String getTheme() { return theme != null ? theme : "default"; }
    public void setTheme(String theme) { this.theme = theme; }
    
    // --- レベルアップ関連 ---
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

    // ★★★ XPのゲッターとセッター ★★★
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
}