package com.example.demo.dto;

public class ExerciseData {
    private String name;
    private String difficulty; // "初級", "中級", "上級"
    private String howTo;
    private String points;

    public ExerciseData(String name, String difficulty, String howTo, String points) {
        this.name = name;
        this.difficulty = difficulty;
        this.howTo = howTo;
        this.points = points;
    }

    public String getName() { return name; }
    public String getDifficulty() { return difficulty; }
    public String getHowTo() { return howTo; }
    public String getPoints() { return points; }

    // 表示用にフルネームを返す
    public String getFullName() {
        return name + " (" + difficulty + ")";
    }
}