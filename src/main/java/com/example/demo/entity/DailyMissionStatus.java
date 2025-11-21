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
import jakarta.persistence.Table;

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
    private LocalDate date; // ミッションが割り当てられた日付

    @Column(nullable = false)
    private String missionType; // 例: "TRAINING_LOG", "COMMUNITY_POST"

    @Column(nullable = false)
    private int requiredCount; // 必要な達成回数/量

    @Column(nullable = false)
    private int currentCount = 0; // 現在の進捗

    @Column(nullable = false)
    private boolean completed = false; // 完了フラグ

    @Column(nullable = false)
    private int rewardExp = 100; // 報酬経験値 (ミッションごとに変更可能)

    // コンストラクタ
    public DailyMissionStatus() {}

    public DailyMissionStatus(User user, LocalDate date, String missionType, int requiredCount, int rewardExp) {
        this.user = user;
        this.date = date;
        this.missionType = missionType;
        this.requiredCount = requiredCount;
        this.rewardExp = rewardExp;
    }

    // 進捗をインクリメントし、完了をチェックするメソッド
    public boolean incrementProgress() {
        if (!completed) {
            this.currentCount = Math.min(this.currentCount + 1, this.requiredCount);
            if (this.currentCount >= this.requiredCount) {
                this.completed = true;
                return true; // 完了した場合
            }
        }
        return false; // 完了していない場合
    }

    // getter, setter (省略)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getMissionType() { return missionType; }
    public void setMissionType(String missionType) { this.missionType = missionType; }
    public int getRequiredCount() { return requiredCount; }
    public void setRequiredCount(int requiredCount) { this.requiredCount = requiredCount; }
    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) { this.currentCount = currentCount; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public int getRewardExp() { return rewardExp; }
    public void setRewardExp(int rewardExp) { this.rewardExp = rewardExp; }
}