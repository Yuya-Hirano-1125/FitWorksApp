package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByResetPasswordToken(String resetPasswordToken);
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    // 元のメソッド（Top20）
    List<User> findTop20ByOrderByLevelDescExperiencePointsDesc();

    // ★追加: 全員をレベル・経験値の高い順に取得するメソッド（Top20を外して All に変更）
    List<User> findAllByOrderByLevelDescExperiencePointsDesc();
}