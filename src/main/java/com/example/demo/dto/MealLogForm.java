package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MealLogForm {

    @NotBlank(message = "食事の種類を選択してください")
    private String mealType;

    @NotBlank(message = "食事内容を記入してください")
    private String content;

    @NotBlank(message = "日付を入力してください")
    private String date; // YYYY-MM-DD 形式

    @NotBlank(message = "時刻を入力してください")
    private String time; // HH:MM 形式

    @NotNull(message = "カロリーを入力してください")
    @Min(value = 0, message = "カロリーは0以上である必要があります")
    @Max(value = 5000, message = "カロリーが高すぎます（最大5000kcal）")
    private Integer calories;

    @Min(value = 0, message = "タンパク質は0以上である必要があります")
    @Max(value = 500, message = "タンパク質が多すぎます")
    private Double protein;

    @Min(value = 0, message = "脂質は0以上である必要があります")
    @Max(value = 500, message = "脂質が多すぎます")
    private Double fat;

    @Min(value = 0, message = "炭水化物は0以上である必要があります")
    @Max(value = 500, message = "炭水化物が多すぎます")
    private Double carbohydrate;
    
    private String imageUrl; // 画像URL
}