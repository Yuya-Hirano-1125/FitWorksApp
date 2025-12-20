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

    @Column(nullable = false)
    private LocalDate recordDate; // 記録日

    @Column(nullable = false)
    private String type; // "WEIGHT" or "CARDIO"

    // フリーウェイト用フィールド
    private String exerciseName;
    
    // ★追加: 部位 (称号機能判定用: "胸", "背中", "脚" など)
    private String bodyPart; 
    
    private Integer sets;
    private Integer reps;
    private Double weight; // kg

    // 有酸素運動用フィールド
    private String cardioType;
    private Integer durationMinutes; // 分
    private Double distanceKm; // km

    // ★追加: メモ欄
    @Column(length = 500)
    private String memo;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}