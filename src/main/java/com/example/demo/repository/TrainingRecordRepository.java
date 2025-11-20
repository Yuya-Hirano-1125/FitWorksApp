package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.TrainingRecord;

@Repository
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {
    
    // 特定のユーザーの特定月日の記録を取得
    List<TrainingRecord> findByUser_IdAndRecordDate(Long userId, LocalDate date);
    
    // 特定のユーザーの特定月の記録を取得 (カレンダー表示用)
    List<TrainingRecord> findByUser_IdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}