package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 作成日時の新しい順に取得
    List<Post> findAllByOrderByCreatedAtDesc();
}