package com.example.demo.controller;

import java.util.List;

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
    
    // NGワードのリスト
    private static final List<String> NG_WORDS = List.of("死ね", "バカ", "アホ", "殺す", "暴力");

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
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        String cleanTitle = filterNgWords(title);
        String cleanContent = filterNgWords(content);

        Post post = new Post();
        post.setTitle(cleanTitle);
        post.setContent(cleanContent);
        post.setAuthor(user);
        postRepository.save(post);
        return "redirect:/community";
    }

    // 投稿詳細＆コメント表示
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        // ★ 修正: コメントも一緒に取得するメソッドに変更
        Post post = postRepository.findByIdWithComments(id).orElseThrow();
        model.addAttribute("post", post);
        return "community/detail";
    }

    // コメント投稿処理
    @PostMapping("/{id}/comment")
    public String createComment(@PathVariable Long id, @RequestParam String content, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        Post post = postRepository.findById(id).orElseThrow();
        
        String cleanContent = filterNgWords(content);

        Comment comment = new Comment();
        comment.setContent(cleanContent);
        comment.setAuthor(user);
        comment.setPost(post);
        commentRepository.save(comment);
        
        return "redirect:/community/" + id;
    }

    // NGワードフィルタリング
    private String filterNgWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String filteredText = text;
        for (String ngWord : NG_WORDS) {
            if (filteredText.contains(ngWord)) {
                filteredText = filteredText.replace(ngWord, "***");
            }
        }
        return filteredText;
    }
}
