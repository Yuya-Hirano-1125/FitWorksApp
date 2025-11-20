package com.example.demo.repository;

import java.util.List;
import java.util.Optional; // ★追加

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // ★追加
import org.springframework.data.repository.query.Param; // ★追加

import com.example.demo.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();

    // ★追加: IDで検索する際、コメントも一緒に取得（JOIN FETCH）するクエリ
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :id")
    Optional<Post> findByIdWithComments(@Param("id") Long id);
}