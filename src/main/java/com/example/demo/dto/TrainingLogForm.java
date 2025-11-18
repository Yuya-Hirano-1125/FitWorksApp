package com.example.demo.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class TrainingLogForm {
    
    // 共通
    private LocalDate recordDate;
    private String type; // "WEIGHT" or "CARDIO"

    // フリーウェイト用
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private Double weight; // kg

    // 有酸素運動用
    private String cardioType;
    private Integer durationMinutes; // 分
    private Double distanceKm; // km
}