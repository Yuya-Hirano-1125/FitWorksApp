package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.MealRecord;
import com.example.demo.entity.User;

@Repository
public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    /** 特定のユーザーの食事記録を最新順で取得 */
    List<MealRecord> findByUserOrderByMealDateTimeDesc(User user);
    
    // TODO: AIコーチングのために、最新の数日間のデータを取得するメソッドなどを追加できます
}