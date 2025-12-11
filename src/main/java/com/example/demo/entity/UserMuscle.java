package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class UserMuscle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 部位名 (胸, 背中, 脚, 肩, 腕, 腹筋, その他)
    private String targetPart;

    // ユーザーが付けた名前 (例: "胸筋太郎")
    private String customName;

    private int level = 1;
    private int xp = 0;

    public UserMuscle() {}

    public UserMuscle(User user, String targetPart, String customName) {
        this.user = user;
        this.targetPart = targetPart;
        this.customName = customName;
        this.level = 1;
        this.xp = 0;
    }

    // 次のレベルまでの必要XP
    public int getRequiredXp() {
        return level * 100;
    }

    // 経験値を加算してレベルアップ判定
    public boolean addXp(int earnedXp) {
        this.xp += earnedXp;
        boolean leveledUp = false;
        while (this.xp >= getRequiredXp()) {
            this.xp -= getRequiredXp();
            this.level++;
            leveledUp = true;
        }
        return leveledUp;
    }
    
    // 進捗率(%)
    public int getProgressPercent() {
        return (int) (((double) xp / getRequiredXp()) * 100);
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTargetPart() { return targetPart; }
    public void setTargetPart(String targetPart) { this.targetPart = targetPart; }
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
}