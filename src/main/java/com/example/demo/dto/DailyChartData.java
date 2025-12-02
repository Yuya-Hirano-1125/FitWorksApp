package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyChartData {
    private String date;       // 表示用日付 (例: "12/01")
    private int cardioMinutes; // 有酸素運動の時間（分）
    private int weightMinutes; // フリーウェイトの時間（分）
    private Double bodyWeight; // 体重 (kg)

    public DailyChartData(String date, int cardioMinutes, int weightMinutes, Double bodyWeight) {
        this.date = date;
        this.cardioMinutes = cardioMinutes;
        this.weightMinutes = weightMinutes;
        this.bodyWeight = bodyWeight;
    }
}