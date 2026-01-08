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
import com.example.demo.entity.CharacterEntity; // ★追加
import com.example.demo.entity.User;
import com.example.demo.repository.CharacterRepository; // ★追加
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.SmsService;
import com.example.demo.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SmsService smsService;

    // ★追加: CharacterRepositoryを注入
    @Autowired
    private CharacterRepository characterRepository;

 // ★追加: アプリ起動時(ルートパス)に start.html を表示
    @GetMapping("/")
    public String showStart() {
        // もし既にログイン済みなら、ホーム画面へリダイレクトさせる（不要ならこのifブロックは削除可）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        
        return "start"; // templates/start.html を表示
    }
    
    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam("birthDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.registerNewUser(username, email, password, birthDate);
            redirectAttributes.addFlashAttribute("successMessage", "登録が完了しました。ログインしてください。");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/home")
    public String showHome(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // ログインユーザー情報の取得
        User user = userService.findByUsername(userDetails.getUsername());
        
        // 既存のユーザー情報設定
        model.addAttribute("username", user.getUsername());
        model.addAttribute("level", user.getLevel());
        model.addAttribute("experiencePoints", user.getExperiencePoints());
        model.addAttribute("requiredXp", user.calculateRequiredXp());
        model.addAttribute("progressPercent", user.getProgressPercent());
        model.addAttribute("userTitle", user.getDisplayTitle());
        
        // 背景画像の判定
     // デフォルト値("fire-original")の設定を削除し、未設定ならnullのままにする
        String bgImage = user.getSelectedBackground();
        // if (bgImage == null || bgImage.isEmpty()) { bgImage = "fire-original"; } // ←削除
        model.addAttribute("selectedBackground", bgImage);

        // 背景解放チェック
        BackgroundUnlockDto unlockDto = userService.checkNewBackgroundUnlocks(user.getUsername());
        model.addAttribute("backgroundUnlocks", unlockDto);

        // ミッション達成状況
        boolean missionAchieved = false;
        if (user.getLastMissionCompletionDate() != null && 
            user.getLastMissionCompletionDate().equals(LocalDate.now()) && 
            (user.getIsRewardClaimedToday() == null || !user.getIsRewardClaimedToday())) {
            missionAchieved = true;
        }
        model.addAttribute("missionAchieved", missionAchieved);

        // ▼▼▼▼▼ キャラクター画像決定ロジック (追加) ▼▼▼▼▼
        String characterImagePath = null; // 初期値をnullに

        if (user.getSelectedCharacterId() != null) {
            // 選択されたIDからキャラクター情報を取得
            CharacterEntity selectedChar = characterRepository.findById(user.getSelectedCharacterId()).orElse(null);
            
            if (selectedChar != null) {
                characterImagePath = selectedChar.getImagePath();
            }
        } 
        model.addAttribute("characterImagePath", characterImagePath);
        return "misc/home";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password/email")
    public String processForgotPasswordEmail(@RequestParam("email") String email,
                                             @RequestParam("birthDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
                                             RedirectAttributes redirectAttributes) {
        boolean sent = userService.sendAuthCodeByEmail(email, birthDate);
        if (sent) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("message", "認証コードをメールで送信しました。");
            return "redirect:/verify-code";
        } else {
            redirectAttributes.addFlashAttribute("error", "メールアドレスまたは生年月日が一致しません。");
            return "redirect:/forgot-password";
        }
    }

    @PostMapping("/forgot-password/sms")
    public String processForgotPasswordSms(@RequestParam("phoneNumber") String phoneNumber,
                                           RedirectAttributes redirectAttributes) {
        boolean sent = userService.processForgotPasswordBySms(phoneNumber);
        if (sent) {
            redirectAttributes.addFlashAttribute("phoneNumber", phoneNumber);
            redirectAttributes.addFlashAttribute("message", "認証コードをSMSで送信しました。");
            return "redirect:/verify-code";
        } else {
            redirectAttributes.addFlashAttribute("error", "電話番号が登録されていません。");
            return "redirect:/forgot-password";
        }
    }
    
    @GetMapping("/verify-code")
    public String showVerifyCodeForm(@RequestParam(value = "email", required = false) String email,
                                     @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                     Model model) {
        model.addAttribute("email", email);
        model.addAttribute("phoneNumber", phoneNumber);
        return "auth/verify-code";
    }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam(value = "email", required = false) String email,
                             @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                             @RequestParam("code") String code,
                             RedirectAttributes redirectAttributes) {
        
        String resetToken = null;
        if (email != null && !email.isEmpty()) {
            resetToken = userService.verifyAuthCode(email, code);
        } else if (phoneNumber != null && !phoneNumber.isEmpty()) {
            // SMS認証の実装が必要であればここに記述
        }

        if (resetToken != null) {
            return "redirect:/reset-password?token=" + resetToken;
        } else {
            redirectAttributes.addFlashAttribute("error", "認証コードが正しくないか、有効期限切れです。");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/verify-code";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        User user = userService.getByResetPasswordToken(token);
        if (user == null) {
            model.addAttribute("error", "無効なトークンです。");
        } else {
            model.addAttribute("token", token);
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       RedirectAttributes redirectAttributes) {
        boolean updated = userService.updatePasswordByToken(token, password);
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "パスワードを再設定しました。ログインしてください。");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "パスワードの再設定に失敗しました（有効期限切れなど）。");
            return "redirect:/forgot-password";
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
                    // 仮のユーザー名を設定
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