package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.AuthProvider; // Enumのimportを忘れずに
import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 基本検索
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // パスワードリセット用
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
    
    // --- ★ここから既存機能のメソッド ---
    
    // ランキング用
    List<User> findTop20ByOrderByLevelDescXpDesc();
    List<User> findAllByOrderByLevelDescXpDesc();

    // フレンド検索用 (これが消えていました)
    List<User> findByUsernameContainingIgnoreCase(String keyword);

    // --- ★ここから新規追加機能 (ソーシャル/SMSログイン) ---
    
    // 電話番号ログイン用
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    // LINE/Appleログイン用
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}