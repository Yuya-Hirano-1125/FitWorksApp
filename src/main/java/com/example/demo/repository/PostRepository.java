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

    // ★修正箇所: メソッド名を「findAllWithLikes」という独自の名前に変更しました。
    // これで確実に @Query の中身（JOIN FETCH）が実行されるようになります。
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.likedBy LEFT JOIN FETCH p.author ORDER BY p.createdAt DESC")
    List<Post> findAllWithLikes();

    // 詳細画面用（変更なし）
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments LEFT JOIN FETCH p.likedBy WHERE p.id = :id")
    Optional<Post> findByIdWithComments(@Param("id") Long id);
    
}