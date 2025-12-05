package com.example.demo.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

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
import com.example.demo.service.MissionService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/community")
public class CommunityController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MissionService missionService; // ✅ 追加

    private static final List<String> NG_WORDS = List.of("死ね", "バカ", "アホ", "殺す", "暴力");

    public CommunityController(PostRepository postRepository,
                               CommentRepository commentRepository,
                               UserRepository userRepository,
                               UserService userService,
                               MissionService missionService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.missionService = missionService; // ✅ 追加
    }

    // 一覧表示
    @GetMapping
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<Post> posts = postRepository.findAllWithLikes();
        model.addAttribute("posts", posts);

        if (userDetails != null) {
            User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            model.addAttribute("currentUser", currentUser);
        }

        return "community/index";
    }

    // 投稿処理
    @PostMapping("/post")
    public String createPost(@RequestParam String title,
                             @RequestParam String content,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        String cleanTitle = filterNgWords(title);
        String cleanContent = filterNgWords(content);

        Post post = new Post();
        post.setTitle(cleanTitle);
        post.setContent(cleanContent);
        post.setAuthor(user);
        postRepository.save(post);

        // ✅ ミッション進捗更新
        missionService.updateMissionProgress(user.getId(), "COMMUNITY_POST");

        return "redirect:/community";
    }

    // 詳細表示
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postRepository.findByIdWithComments(id).orElseThrow();
        model.addAttribute("post", post);

        if (userDetails != null) {
            User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            model.addAttribute("currentUser", currentUser);
        }
        return "community/detail";
    }

    // コメント投稿
    @PostMapping("/{id}/comment")
    public String createComment(@PathVariable Long id,
                                @RequestParam String content,
                                @AuthenticationPrincipal UserDetails userDetails) {
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

    // いいね機能
    @PostMapping("/{id}/like")
    public String toggleLike(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             HttpServletRequest request) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Post post = postRepository.findByIdWithComments(id).orElseThrow();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        User existingLike = post.getLikedBy().stream()
                .filter(u -> u.getId().equals(user.getId()))
                .findFirst()
                .orElse(null);

        if (existingLike != null) {
            post.getLikedBy().remove(existingLike);
        } else {
            post.getLikedBy().add(user);
        }

        postRepository.save(post);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/community");
    }

    private String filterNgWords(String text) {
        if (text == null || text.isEmpty()) return text;
        String filteredText = text;
        for (String ngWord : NG_WORDS) {
            if (filteredText.contains(ngWord)) {
                filteredText = filteredText.replace(ngWord, "***");
            }
        }
        return filteredText;
    }
}
