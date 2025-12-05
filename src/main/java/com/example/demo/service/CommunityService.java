package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.PostRepository;

@Service
public class CommunityService {

    private final PostRepository postRepository;

    public CommunityService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * 投稿を作成する
     */
    public Post createPost(User user, String title, String content) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(user);
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    /**
     * 投稿一覧を取得（新しい順）
     */
    public List<Post> getAllPosts() {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * 投稿をIDで取得
     */
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
    }
}
