package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "training_record")
public class TrainingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // マスタデータ(Exercise)への参照
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    // ★日付のみ (年・月・日) を扱うために LocalDate 型を使用
    // (データベース上では DATE 型になります)
    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false)
    private String type; // "WEIGHT" or "CARDIO"

    private String exerciseName;
    
    // bodyPartフィールドは削除し、getBodyPartメソッドで代替
    
    private Integer sets;
    private Integer reps;
    private Double weight;

    // 有酸素運動用フィールド
    private String cardioType;
    private Integer durationMinutes;
    private Double distanceKm;

    @Column(length = 500)
    private String memo;

    // 作成日時 (こちらは日時まで保持)
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * マスタデータ(Exercise)を参照して部位名を返す便利メソッド
     */
    public String getBodyPart() {
        if (this.exercise != null) {
            return this.exercise.getBodyPartGroup();
        }
        if ("CARDIO".equals(this.type)) {
            return "有酸素";
        }
        return "その他";
    }
}