package com.example.demo.controller;

import java.util.Base64;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.WebAuthnService;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;

@RestController
@RequestMapping("/api/webauthn")
public class WebAuthnController {

    @Autowired
    private WebAuthnService webAuthnService;

    @Autowired
    private UserRepository userRepository;

    // --- 登録フロー: オプション取得 ---
    @GetMapping("/register/options")
    public ResponseEntity<?> getRegisterOptions(HttpSession session, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        Challenge challenge = webAuthnService.generateChallenge();
        // チャレンジをセッションに保存（Base64URLエンコードして保存）
        session.setAttribute("WEBAUTHN_CHALLENGE", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge.getValue()));

        return ResponseEntity.ok(Map.of(
            "challenge", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge.getValue()),
            "user", Map.of(
                "id", Base64.getUrlEncoder().withoutPadding().encodeToString(user.getId().toString().getBytes()),
                "name", user.getUsername(),
                "displayName", user.getUsername()
            )
        ));
    }

    // --- 登録フロー: 完了 ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body, HttpSession session, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String challengeStr = (String) session.getAttribute("WEBAUTHN_CHALLENGE");
            if (challengeStr == null) {
                return ResponseEntity.badRequest().body("Challenge not found");
            }
            Challenge challenge = new DefaultChallenge(Base64.getUrlDecoder().decode(challengeStr));
            
            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
            String clientDataJSON = body.get("clientDataJSON");
            String attestationObject = body.get("attestationObject");
            String deviceName = body.getOrDefault("deviceName", "My Device");

            webAuthnService.register(clientDataJSON, attestationObject, challenge, user, deviceName);
            
            session.removeAttribute("WEBAUTHN_CHALLENGE");
            return ResponseEntity.ok("Registered successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    // --- ログインフロー: オプション取得 ---
    @GetMapping("/login/options")
    public ResponseEntity<?> getLoginOptions(HttpSession session) {
        Challenge challenge = webAuthnService.generateChallenge();
        session.setAttribute("WEBAUTHN_CHALLENGE", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge.getValue()));
        
        return ResponseEntity.ok(Map.of(
            "challenge", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge.getValue())
        ));
    }
    
    // --- ログインフロー: 完了 ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session, HttpServletRequest request) {
        try {
            String challengeStr = (String) session.getAttribute("WEBAUTHN_CHALLENGE");
            if (challengeStr == null) {
                return ResponseEntity.badRequest().body("Challenge not found");
            }
            Challenge challenge = new DefaultChallenge(Base64.getUrlDecoder().decode(challengeStr));

            String credentialId = body.get("credentialId");
            String clientDataJSON = body.get("clientDataJSON");
            String authenticatorData = body.get("authenticatorData");
            String signature = body.get("signature");
            String userHandle = body.get("userHandle");

            User user = webAuthnService.authenticate(credentialId, userHandle, clientDataJSON, authenticatorData, signature, challenge);
            
            // 認証成功 -> Spring Securityコンテキストに設定
            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
            
            session.removeAttribute("WEBAUTHN_CHALLENGE");
            
            return ResponseEntity.ok(Map.of("status", "success", "username", user.getUsername()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }
}