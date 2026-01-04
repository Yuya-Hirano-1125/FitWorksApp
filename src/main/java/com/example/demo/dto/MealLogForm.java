package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class MealLogForm {

    @NotEmpty(message = "日付を入力してください")
    private String date;

    @NotEmpty(message = "時間を入力してください")
    private String time;

    @NotEmpty(message = "食事タイプを選択してください")
    private String mealType; // 朝食, 昼食, 夕食, 間食

    @NotEmpty(message = "内容を入力してください")
    private String content;

    @NotNull(message = "カロリーを入力してください")
    @PositiveOrZero(message = "0以上の値を入力してください")
    private Integer calories;

    @PositiveOrZero
    private Double protein;

    @PositiveOrZero
    private Double fat;

    @PositiveOrZero
    private Double carbohydrate;

    // 画像URL（DB保存用、または表示用）
    private String imageUrl;

    // ★追加: アップロードされたファイルを受け取るフィールド
    private MultipartFile imageFile;
}