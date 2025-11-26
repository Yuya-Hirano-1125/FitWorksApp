package com.example.demo.dto;

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
    private Integer calories;

    @Min(value = 0, message = "タンパク質は0以上である必要があります")
    private Double protein;

    @Min(value = 0, message = "脂質は0以上である必要があります")
    private Double fat;

    @Min(value = 0, message = "炭水化物は0以上である必要があります")
    private Double carbohydrate;
    
    private String imageUrl; // 画像URL
}