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

    // ★追加: リセットトークンでユーザーを検索
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    List<User> findTop20ByOrderByLevelDescExperiencePointsDesc();
}