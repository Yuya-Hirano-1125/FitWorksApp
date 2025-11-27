package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List; // 追加

import lombok.Data;

@Data
public class TrainingLogForm {
    
    // 共通
    private LocalDate recordDate;
    private String type; // "WEIGHT" or "CARDIO"

    // フリーウェイト用 (既存フィールドは互換性のために残すか、単発登録で利用)
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private Double weight; // kg

    // ★ 追加: セットごとの詳細リスト
    private List<SetDetail> setList;

    @Data
    public static class SetDetail {
        private Double weight;
        private Integer reps;
    }

    // 有酸素運動用
    private String cardioType;
    private Integer durationMinutes; // 分
    private Double distanceKm; // km
}