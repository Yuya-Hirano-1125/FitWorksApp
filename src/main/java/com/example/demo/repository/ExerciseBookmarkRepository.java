package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ExerciseBookmark;
import com.example.demo.entity.User;

@Repository
public interface ExerciseBookmarkRepository extends JpaRepository<ExerciseBookmark, Long> {
    List<ExerciseBookmark> findByUserOrderByIdDesc(User user);
    Optional<ExerciseBookmark> findByUserAndExerciseName(User user, String exerciseName);
}