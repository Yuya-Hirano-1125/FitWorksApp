package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AppTitle;
import com.example.demo.entity.User;
import com.example.demo.entity.UserTitle;

public interface UserTitleRepository extends JpaRepository<UserTitle, Long> {
    List<UserTitle> findByUser(User user);
    boolean existsByUserAndTitle(User user, AppTitle title);
}