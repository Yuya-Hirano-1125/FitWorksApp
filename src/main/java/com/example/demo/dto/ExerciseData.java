package com.example.demo.dto;

public class ExerciseData {
    private String name;          // 種目名
    private String targetMuscle;  // ターゲット部位
    private String equipment;     // 使用器具
    private String description;   // 解説・やり方 (追加)

    // コンストラクタ (4つの引数を受け取るように変更)
    public ExerciseData(String name, String targetMuscle, String equipment, String description) {
        this.name = name;
        this.targetMuscle = targetMuscle;
        this.equipment = equipment;
        this.description = description;
    }

    // --- Getter / Setter ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTargetMuscle() { return targetMuscle; }
    public void setTargetMuscle(String targetMuscle) { this.targetMuscle = targetMuscle; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getFullName() { return name; }
}