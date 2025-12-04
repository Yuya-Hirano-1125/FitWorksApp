package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // 【追加】メールアドレスでユーザーを検索するメソッド
    Optional<User> findByEmail(String email);
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
    // ★追加: レベルが高い順、同じならXPが多い順に上位20件を取得
    List<User> findTop20ByOrderByLevelDescExperiencePointsDesc();
}



