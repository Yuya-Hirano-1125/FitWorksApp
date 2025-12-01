package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.MealRecord;
import com.example.demo.entity.User;

@Repository
public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    /** 特定のユーザーの食事記録を最新順で取得 */
    List<MealRecord> findByUserOrderByMealDateTimeDesc(User user);
    
    /** 特定の期間（開始日時～終了日時）の食事記録を取得 */
    List<MealRecord> findByUserAndMealDateTimeBetween(User user, LocalDateTime start, LocalDateTime end);
}