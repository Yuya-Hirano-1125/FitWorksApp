package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Post;

@Repository
public interface CommunityRepository extends JpaRepository<Post, Long> {
    // 必要ならカスタム検索メソッドを追加できます
    // 例: List<Post> findByAuthor(User author);
}
