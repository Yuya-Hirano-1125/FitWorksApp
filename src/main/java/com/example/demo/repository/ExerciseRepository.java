package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    // 種類（WEIGHT/CARDIO）で検索
    List<Exercise> findByType(String type);
    
    // 種目名で検索
    Exercise findByName(String name);
}