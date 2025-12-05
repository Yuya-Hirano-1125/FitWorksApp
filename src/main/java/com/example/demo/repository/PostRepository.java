package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // ★今回の修正箇所: 一覧画面でも「いいね(likedBy)」を使うので、ここでも JOIN FETCH します。
    // DISTINCT は、JOINによってデータが重複して取得されるのを防ぐために付けます。
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.likedBy LEFT JOIN FETCH p.author ORDER BY p.createdAt DESC")
    List<Post> findAllByOrderByCreatedAtDesc();

    // 詳細画面用 (前回修正した部分もそのまま残します)
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments LEFT JOIN FETCH p.likedBy WHERE p.id = :id")
    Optional<Post> findByIdWithComments(@Param("id") Long id);
    
}