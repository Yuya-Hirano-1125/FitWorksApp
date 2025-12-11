package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;

@Repository
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {
    
    // --- 既存機能用メソッド ---
    
    // 全履歴を日付降順で取得
    List<TrainingRecord> findByUser_IdOrderByRecordDateDesc(Long userId);
    
    // 特定の日の記録を取得
    List<TrainingRecord> findByUser_IdAndRecordDate(Long userId, LocalDate recordDate);
    
    // 特定の日の記録数をカウント
    long countByUser_IdAndRecordDate(Long userId, LocalDate recordDate);

    // ★追加: 期間指定で取得（チャート機能などで使用）
    List<TrainingRecord> findByUser_IdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // ★追加: 最新10件を取得（履歴ウィジェットなどで使用）
    List<TrainingRecord> findTop10ByUser_IdOrderByRecordDateDesc(Long userId);


    // --- 称号機能用メソッド ---

    // 1. ユーザーの全トレーニング記録数をカウント（"努力家"などの称号用）
    long countByUser_Id(Long userId);

    // 2. 部位名を含む記録数をカウント（"大胸筋マニア"などの称号用）
    // ※TrainingRecordエンティティが user オブジェクトを持っている前提です
    long countByUserAndBodyPartContaining(User user, String bodyPart);
}