package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.BodyWeightRecord;
import com.example.demo.entity.User;

public interface BodyWeightRecordRepository extends JpaRepository<BodyWeightRecord, Long> {

    /**
     * 指定したユーザーの体重記録を、日付の昇順（古い順）で取得します。
     * グラフ表示用に時系列でデータを並べるために使用します。
     */
    List<BodyWeightRecord> findByUserOrderByDateAsc(User user);
}
























