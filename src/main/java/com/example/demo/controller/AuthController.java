package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    // --- スタート画面（ルートパス） ---
    @GetMapping("/")
    public String index(Authentication authentication) {
        // 既にログインしているユーザーがアクセスした場合はホームへリダイレクトさせる（任意）
        if (authentication != null && authentication.isAuthenticated() && 
           !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        return "start";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               Model model) {
        try {
            userService.registerUser(username, password, email);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
    
    // --- ホーム画面表示 ---
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        User user = userRepository.findByUsername(userDetails.getUsername())
                                  .orElse(null);
        
        if (user == null) {
            return "redirect:/logout";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("level", user.getLevel());
        model.addAttribute("experiencePoints", user.getXp());
        model.addAttribute("requiredXp", user.calculateRequiredXp());
        model.addAttribute("progressPercent", user.getProgressPercent());
        
        // ★追加: 称号を表示するためにモデルに格納
        model.addAttribute("userTitle", user.getDisplayTitle());

        
     // 選択中の背景を取得
        String selectedBackground = user.getSelectedBackground();
        if (selectedBackground == null || selectedBackground.isEmpty()) {
            selectedBackground = "fire-original";
        }
        model.addAttribute("selectedBackground", selectedBackground);
        
        return "misc/home";
    }

    // --- SMS認証用 ---
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

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        // templates/auth/forgot-password.html を表示する
        return "auth/forgot-password";
    }

    @PostMapping("/api/auth/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(@RequestParam String phoneNumber, 
                                       @RequestParam String code, 
                                       HttpServletRequest request) {
        if (smsService.verifyOtp(phoneNumber, code)) {
            User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setPhoneNumber(phoneNumber);
                    newUser.setProvider(AuthProvider.PHONE);
                    newUser.setUsername("phone_" + phoneNumber.substring(phoneNumber.length() - 4)); 
                    return userRepository.save(newUser);
                });

            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid verification code");
    }
}