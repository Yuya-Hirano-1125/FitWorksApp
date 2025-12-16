package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Authenticator;
import com.example.demo.entity.User;

public interface AuthenticatorRepository extends JpaRepository<Authenticator, Long> {
    List<User> findByUser(User user);
    Optional<Authenticator> findByCredentialId(String credentialId);
}