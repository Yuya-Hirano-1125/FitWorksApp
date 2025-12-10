package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // ★homeメソッドで追加
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // ★homeメソッドで追加
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.SmsService;
import com.example.demo.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserRepository userRepository;

    // 既存：ログイン画面表示
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // 既存：登録画面表示
    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    // 既存：ユーザー登録処理
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               Model model) {
        try {
            // UserServiceのregisterUser(String, String, String)は、
            // 互換性のため別途追加したラッパーメソッドです
            userService.registerUser(username, password, email);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
    
    // --- ★ホーム画面表示メソッド（今回追加）---
    /**
     * 認証成功後、/homeへのリクエストを処理し、ホーム画面を表示します。
     * テンプレートに必要なユーザーデータをModelに設定します。
     */
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        // ユーザー名から最新のユーザーエンティティを取得
        User user = userRepository.findByUsername(userDetails.getUsername())
                                  .orElse(null);
        
        if (user == null) {
            // DBにユーザーが見つからない場合はログアウト
            return "redirect:/logout";
        }

        // home.html (Thymeleaf) が期待するモデル属性を設定
        model.addAttribute("username", user.getUsername());
        model.addAttribute("level", user.getLevel());
        model.addAttribute("experiencePoints", user.getXp());
        model.addAttribute("requiredXp", user.calculateRequiredXp()); // Userエンティティのメソッドを利用
        model.addAttribute("progressPercent", user.getProgressPercent()); // Userエンティティのメソッドを利用

        // テンプレートのパスを返す (src/main/resources/templates/misc/home.html)
        return "misc/home";
    }

    // --- SMS認証用エンドポイント ---

    // 既存：SMS送信API
    @PostMapping("/api/auth/send-otp")
    @ResponseBody
    public ResponseEntity<?> sendOtp(@RequestParam String phoneNumber) {
        try {
            smsService.sendOtp(phoneNumber);
            return ResponseEntity.ok("SMS sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send SMS: " + e.getMessage());
        }
    }

    // 既存：コード検証＆ログインAPI
    @PostMapping("/api/auth/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(@RequestParam String phoneNumber, 
                                       @RequestParam String code, 
                                       HttpServletRequest request) {
        if (smsService.verifyOtp(phoneNumber, code)) {
            // 認証成功：ユーザー検索または新規作成
            User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setPhoneNumber(phoneNumber);
                    newUser.setProvider(AuthProvider.PHONE);
                    newUser.setUsername("phone_" + phoneNumber.substring(phoneNumber.length() - 4)); 
                    return userRepository.save(newUser);
                });

            // 手動でSpring Securityのセッションにログインさせる
            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            // セッションにセキュリティコンテキストを保存
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid verification code");
    }
}