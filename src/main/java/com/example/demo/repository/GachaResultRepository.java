package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.GachaResult;

@Repository
public interface GachaResultRepository extends JpaRepository<GachaResult, Long> {

    // ユーザーIDで検索し、IDの降順（新しい順）で取得する
    // 元のJDBC実装が "ORDER BY id DESC" していたため、同じ挙動を維持します
    @Query("SELECT g FROM GachaResult g WHERE g.userId = :userId ORDER BY g.id DESC")
    List<GachaResult> findByUserId(Long userId);

}