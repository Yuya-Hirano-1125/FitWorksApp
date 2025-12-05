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

    /**
     * 指定ユーザーの指定日付に紐づく全てのミッションステータスを取得
     *
     * @param user ユーザー
     * @param date 日付
     * @return ミッションステータス一覧
     */
    List<DailyMissionStatus> findByUserAndDate(User user, LocalDate date);

    /**
     * 指定ユーザーの指定日付・ミッションタイプに紐づくミッションステータスを取得
     *
     * @param user ユーザー
     * @param date 日付
     * @param missionType ミッションタイプ (例: TRAINING_LOG, COMMUNITY_POST)
     * @return ミッションステータス (存在しない場合は Optional.empty)
     */
    Optional<DailyMissionStatus> findByUserAndDateAndMissionType(User user, LocalDate date, String missionType);
}
