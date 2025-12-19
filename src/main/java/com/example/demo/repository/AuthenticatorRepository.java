package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Authenticator;
import com.example.demo.entity.User;

@Repository
public interface AuthenticatorRepository extends JpaRepository<Authenticator, Long> {
    Optional<Authenticator> findByCredentialId(String credentialId);
    
    // ★以下の行を追加してください★
    List<Authenticator> findByUser(User user);
}