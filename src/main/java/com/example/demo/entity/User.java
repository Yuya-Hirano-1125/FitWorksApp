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

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email; 

    // ★ 修正1: int型からInteger型に変更し、DBのNULL値に対応
    private Integer level = 1; // 初期レベル
    private Integer experiencePoints = 0; // 初期XP

    // JPAの要件: 引数なしのコンストラクタ (初期値設定の安全性を高めるため)
    public User() {
        if (this.level == null) this.level = 1;
        if (this.experiencePoints == null) this.experiencePoints = 0;
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

    // ★ 修正2: 戻り値を Integer に変更し、null安全性を確保 (nullの場合は1を返す)
    public Integer getLevel() { 
        return level != null ? level : 1; 
    }
    public void setLevel(Integer level) { this.level = level; }

    // ★ 修正3: 戻り値を Integer に変更し、null安全性を確保 (nullの場合は0を返す)
    public Integer getExperiencePoints() { 
        return experiencePoints != null ? experiencePoints : 0; 
    }
    public void setExperiencePoints(Integer experiencePoints) { this.experiencePoints = experiencePoints; }

    // --- レベルアップ関連 ---
    // null安全なGetter (getLevel()) を使用するため修正
    public int calculateRequiredXp() {
        int currentLevel = getLevel();
        return 1000 + (currentLevel - 1) * 200;
    }

    public void addXp(int xp) {
        // null安全なGetter (getExperiencePoints()) を使用
        int currentXp = getExperiencePoints(); 
        this.experiencePoints = currentXp + xp;
        
        while (this.experiencePoints >= calculateRequiredXp()) {
            this.experiencePoints -= calculateRequiredXp();
            this.level++;
        }
    }

    // null安全なGetter (getExperiencePoints()) を使用するため修正
    public int getProgressPercent() {
        int currentXp = getExperiencePoints();
        int requiredXp = calculateRequiredXp();
        if (requiredXp == 0) return 0; // ゼロ除算防止
        return (int)(((double) currentXp / requiredXp) * 100);
    }
}