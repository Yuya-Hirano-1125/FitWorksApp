package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "exercises")
@Data
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // 種目名
    private String targetPart;  // 効く部位（例：大胸筋、全身）
    private String equipment;   // 器具（例：バーベル、自重）
    private String description; // 説明
    private String difficulty;  // 難易度（初級、中級、上級）

    // 分類用フィールド
    // WEIGHT（フリーウェイト/マシン）、CARDIO（有酸素）
    private String type; 
    
    // グループ分け用（例：胸、背中、脚... 有酸素の場合はnullまたは"有酸素"）
    private String bodyPartGroup; 
}