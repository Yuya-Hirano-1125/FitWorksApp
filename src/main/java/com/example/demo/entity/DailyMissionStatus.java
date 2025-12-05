package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "daily_mission_status")
public class DailyMissionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date; 

    @Column(nullable = false)
    private String missionType; // 例: "TRAINING_LOG", "COMMUNITY_POST"

    @Column(nullable = false)
    private String description; 

    @Column(nullable = false)
    private int requiredCount;   // 目標回数

    @Column(nullable = false)
    private int currentCount = 0; // 現在の進捗

    @Column(nullable = false)
    private boolean completed = false; 

    @Column(nullable = false)
    private boolean rewardClaimed = false; 

    @Column(nullable = false)
    private int rewardExp;       // 報酬XP

    // --- コンストラクタ ---
    public DailyMissionStatus() {}

    public DailyMissionStatus(User user, LocalDate date, String missionType, String description, int requiredCount, int rewardExp) {
        this.user = user;
        this.date = date;
        this.missionType = missionType;
        this.description = description;
        this.requiredCount = requiredCount;
        this.rewardExp = rewardExp;
        this.currentCount = 0;
        this.completed = false;
        this.rewardClaimed = false;
    }

    // --- 進捗をインクリメントし、完了状態をチェックするメソッド ---
    public boolean incrementProgress() {
        if (!completed) {
            this.currentCount = Math.min(this.currentCount + 1, this.requiredCount);
            if (this.currentCount >= this.requiredCount) {
                this.completed = true;
                return true;
            }
        }
        return false;
    }

    // --- Getter / Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getMissionType() { return missionType; }
    public void setMissionType(String missionType) { this.missionType = missionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getRequiredCount() { return requiredCount; }
    public void setRequiredCount(int requiredCount) { this.requiredCount = requiredCount; }

    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) { this.currentCount = currentCount; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isRewardClaimed() { return rewardClaimed; }
    public void setRewardClaimed(boolean rewardClaimed) { this.rewardClaimed = rewardClaimed; }

    public int getRewardExp() { return rewardExp; }
    public void setRewardExp(int rewardExp) { this.rewardExp = rewardExp; }
}
