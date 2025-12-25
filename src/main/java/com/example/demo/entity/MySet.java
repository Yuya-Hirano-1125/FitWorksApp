package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class MySet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name; // マイセット名

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> exerciseNames; // 種目名のリスト

    // コンストラクタ
    public MySet() {}

    public MySet(User user, String name, List<String> exerciseNames) {
        this.user = user;
        this.name = name;
        this.exerciseNames = exerciseNames;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getExerciseNames() { return exerciseNames; }
    public void setExerciseNames(List<String> exerciseNames) { this.exerciseNames = exerciseNames; }
}