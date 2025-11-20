package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 // ★★★ ここに経験値(XP)フィールドを追加 ★★★
    private int xp = 0; // 初期値は0
    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email; 

    private int level = 1; // 初期レベル
    private int experiencePoints = 0; // 初期XP

    // --- Getter / Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperiencePoints() { return experiencePoints; }
    public void setExperiencePoints(int experiencePoints) { this.experiencePoints = experiencePoints; }

    // --- レベルアップ関連 ---
    public int calculateRequiredXp() {
        return 1000 + (level - 1) * 200;
    }

    public void addXp(int xp) {
        this.experiencePoints += xp;
        while (this.experiencePoints >= calculateRequiredXp()) {
            this.experiencePoints -= calculateRequiredXp();
            this.level++;
        }
    }

    public int getProgressPercent() {
        return (int)((double) this.experiencePoints / calculateRequiredXp() * 100);
    }
 // ★★★ XPのゲッターとセッターを追加 ★★★

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }
}
