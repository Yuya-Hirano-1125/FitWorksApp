package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;

@Controller
@RequestMapping("/community")
public class CommunityController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommunityController(PostRepository postRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    // 掲示板トップ（一覧表示）
    @GetMapping
    public String index(Model model) {
        model.addAttribute("posts", postRepository.findAllByOrderByCreatedAtDesc());
        return "community/index";
    }

    // 新規投稿処理
    @PostMapping("/post")
    public String createPost(@RequestParam String title, @RequestParam String content, @AuthenticationPrincipal UserDetails userDetails) {
        // ★ 修正: .orElseThrow() を追加して Optional から User を取り出す
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(user);
        postRepository.save(post);
        return "redirect:/community";
    }

    // 投稿詳細＆コメント表示
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        // ここも念のため .orElseThrow() があるか確認（前回のコードには入っていました）
        Post post = postRepository.findById(id).orElseThrow();
        model.addAttribute("post", post);
        return "community/detail";
    }

    // コメント投稿処理
    @PostMapping("/{id}/comment")
    public String createComment(@PathVariable Long id, @RequestParam String content, @AuthenticationPrincipal UserDetails userDetails) {
        // ★ 修正: .orElseThrow() を追加して Optional から User を取り出す
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        Post post = postRepository.findById(id).orElseThrow();
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(user);
        comment.setPost(post);
        commentRepository.save(comment);
        
        return "redirect:/community/" + id;
    }
}