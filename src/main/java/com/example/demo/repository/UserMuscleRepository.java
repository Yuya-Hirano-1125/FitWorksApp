package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;
import com.example.demo.entity.UserMuscle;

@Repository
public interface UserMuscleRepository extends JpaRepository<UserMuscle, Long> {
    List<UserMuscle> findByUser(User user);
    Optional<UserMuscle> findByUserAndTargetPart(User user, String targetPart);
}