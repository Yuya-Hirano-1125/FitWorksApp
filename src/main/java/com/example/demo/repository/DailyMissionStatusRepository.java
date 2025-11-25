package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.DailyMissionStatus;
import com.example.demo.entity.User;

@Repository
public interface DailyMissionStatusRepository extends JpaRepository<DailyMissionStatus, Long> {
    
    // ユーザーと日付で今日のミッションステータスを取得
    List<DailyMissionStatus> findByUserAndDate(User user, LocalDate date);
    
    // ユーザー、日付、ミッションタイプで特定のミッションステータスを取得
    Optional<DailyMissionStatus> findByUserAndDateAndMissionType(User user, LocalDate date, String missionType);
}