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
    
    // 修正済み：Top20をXP順で取得
    List<User> findTop20ByOrderByLevelDescXpDesc();

    // 修正済み：全ユーザーをXP順で取得
    List<User> findAllByOrderByLevelDescXpDesc();
}
