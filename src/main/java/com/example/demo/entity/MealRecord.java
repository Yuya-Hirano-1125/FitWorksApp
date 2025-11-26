package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Data;
@Entity
@Table(name = "meal_records")
@Data
public class MealRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ユーザー
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime mealDateTime; // 食事の日時

    @Column(nullable = false)
    private String mealType; // 例: 朝食, 昼食, 夕食, 間食

    @Column(nullable = false, length = 500)
    private String content; // 食事の内容 (自由記述)

    private Integer calories; // カロリー (kcal)

    private Double protein; // タンパク質 (g)
    
    private Double fat;     // 脂質 (g)
    
    private Double carbohydrate; // 炭水化物 (g)

    private String imageUrl; // 画像URL（オプション）

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
