package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "body_weight_records")
public class BodyWeightRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate date;

    private Double weight;

    // デフォルトコンストラクタ（JPA用）
    public BodyWeightRecord() {
    }

    // 登録用コンストラクタ
    public BodyWeightRecord(User user, LocalDate date, Double weight) {
        this.user = user;
        this.date = date;
        this.weight = weight;
    }
}