package com.example.demo.controller;

import java.time.LocalDate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.BackgroundUnlockDto;
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

    @GetMapping("/")
    public String index(Authentication authentication) {
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
                               // @RequestParam String phoneNumber, ← この行を削除
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
                               Model model) {
        try {
            // phoneNumber 引数を削除して呼び出し
            userService.registerUser(username, password, email, birthDate);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
    
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
        
        model.addAttribute("userTitle", user.getDisplayTitle());
        
        String selectedBackground = user.getSelectedBackground();
        if (selectedBackground == null || selectedBackground.isEmpty()) {
            selectedBackground = "fire-original";
        }
        model.addAttribute("selectedBackground", selectedBackground);
        
        BackgroundUnlockDto backgroundUnlocks = userService.checkNewBackgroundUnlocks(user.getUsername());
        model.addAttribute("backgroundUnlocks", backgroundUnlocks);
        
        return "misc/home";
    }

    // --- パスワードリセットフロー (修正) ---

    // 1. パスワード忘れ画面表示
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    // 2. 本人確認＆認証コード送信処理
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        // メールと生年月日で認証し、コード送信
        boolean result = userService.sendAuthCodeByEmail(email, birthDate);
        
        if (result) {
            // 成功したらメールアドレスを次の画面に引き継いでリダイレクト
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("message", "認証コードをメールで送信しました。");
            return "redirect:/verify-code";
        } else {
            model.addAttribute("error", "メールアドレスまたは生年月日が一致しません。");
            return "auth/forgot-password";
        }
    }

    // 3. 認証コード入力画面表示
    @GetMapping("/verify-code")
    public String verifyCodePage() {
        return "auth/verify-code";
    }

    // 4. 認証コード検証処理
    @PostMapping("/verify-code")
    public String processVerifyCode(@RequestParam String email,
                                    @RequestParam String code,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        // コード検証して再設定用トークンを取得
        String resetToken = userService.verifyAuthCode(email, code);
        
        if (resetToken != null) {
            // 検証成功: トークンを次の画面に渡す (URLパラメータ経由)
            return "redirect:/reset-password?token=" + resetToken;
        } else {
            model.addAttribute("error", "認証コードが正しくないか、有効期限切れです。");
            model.addAttribute("email", email); // メールアドレスを再表示用にセット
            return "auth/verify-code";
        }
    }
    
    // 5. パスワード再設定画面表示
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isEmpty()) {
            return "redirect:/forgot-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }
    
    // 6. パスワード更新処理
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "パスワードが一致しません。");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
        
        boolean success = userService.updatePasswordByToken(token, password);
        
        if (success) {
            return "redirect:/login?resetSuccess";
        } else {
            model.addAttribute("error", "トークンが無効か、有効期限切れです。最初からやり直してください。");
            return "auth/reset-password";
        }
    }

    // --- SMS認証（API） ---
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