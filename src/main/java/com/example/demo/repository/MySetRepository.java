package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.MySet;
import com.example.demo.entity.User;

@Repository
public interface MySetRepository extends JpaRepository<MySet, Long> {
    List<MySet> findByUserOrderByIdDesc(User user);
    MySet findByIdAndUser(Long id, User user);
}