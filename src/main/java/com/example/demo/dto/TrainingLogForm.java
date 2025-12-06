package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.Data;

@Data
public class TrainingLogForm {
    
    // 共通
    private LocalDate recordDate;
    private String type; // "WEIGHT" or "CARDIO"

    // フリーウェイト用
    private String exerciseName;

    @Min(value = 1, message = "セット数は1以上です")
    @Max(value = 50, message = "セット数が多すぎます")
    private Integer sets;

    @Min(value = 1, message = "回数は1以上です")
    @Max(value = 300, message = "回数が多すぎます")
    private Integer reps;

    @Min(value = 0, message = "重量は0kg以上です")
    @Max(value = 500, message = "重量が重すぎます(最大500kg)")
    private Double weight; // kg

    // セットごとの詳細リスト
    private List<SetDetail> setList;

    @Data
    public static class SetDetail {
        @Min(value = 0)
        @Max(value = 500)
        private Double weight;

        @Min(value = 1)
        @Max(value = 300)
        private Integer reps;
    }

    // 有酸素運動用
    private String cardioType;

    @Min(value = 1, message = "時間は1分以上です")
    @Max(value = 1440, message = "時間が長すぎます(最大24時間)")
    private Integer durationMinutes; // 分

    @Min(value = 0, message = "距離は0km以上です")
    @Max(value = 200, message = "距離が長すぎます")
    private Double distanceKm; // km
}