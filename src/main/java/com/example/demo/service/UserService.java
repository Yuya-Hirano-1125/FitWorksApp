package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.MissionStatusDto;
import com.example.demo.entity.User;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final TrainingRecordRepository trainingRecordRepository; 
    private final PasswordEncoder passwordEncoder; 

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TrainingRecordRepository trainingRecordRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.trainingRecordRepository = trainingRecordRepository;
    }
    
    // --- ユーザー登録処理 (修正: メール送信を追加) ---
    @Transactional
    public void registerNewUser(String username, String email, String rawPassword) {
        // 1. ユーザー名の重複チェック
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("そのユーザー名は既に使用されています。");
        }

        // 2. メールアドレスの重複チェック
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("そのメールアドレスは既に使用されています。");
        }

        // 3. ユーザーエンティティの作成
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        
        newUser.setLevel(1);
        newUser.setExperiencePoints(0);
        newUser.setTheme("default");

        // 4. DBに保存
        userRepository.save(newUser);

        // ★追加: 登録完了メールを送信
        sendRegistrationEmail(newUser.getEmail(), newUser.getUsername());
    }

    // ★追加: 登録完了メール送信メソッド
    private void sendRegistrationEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@fitworks.com");
            message.setTo(toEmail);
            message.setSubject("【FitWorks】会員登録ありがとうございます");
            message.setText(username + " 様\n\n" +
                            "FitWorksへの会員登録が完了しました。\n" +
                            "以下のURLからログインしてトレーニングを始めましょう！\n\n" +
                            "ログイン: " + baseUrl + "/login\n\n" +
                            "※このメールにお心当たりがない場合は破棄してください。");
            
            mailSender.send(message);
            System.out.println("登録完了メールを送信しました: " + toEmail);
        } catch (Exception e) {
            // メール送信に失敗しても登録自体はロールバックしないようにcatchしてログ出力に留める
            e.printStackTrace();
            System.err.println("登録完了メールの送信に失敗しました: " + e.getMessage());
        }
    }

    // --- 以下、既存のメソッド ---

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Transactional
    public boolean processForgotPassword(String email) {
        Optional<User> optionalUser = findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            String token = UUID.randomUUID().toString();
            
            user.setResetPasswordToken(token);
            user.setTokenExpiration(LocalDateTime.now().plusHours(24));
            userRepository.save(user);
            
            String resetLink = baseUrl + "/reset-password?token=" + token;
            
            sendResetEmail(user.getEmail(), resetLink);
            
            System.out.println("パスワードリセットリンクを送信しました: " + email);
            return true;
        } else {
            System.out.println("メールアドレスが見つかりません: " + email);
            return false;
        }
    }

    private void sendResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@fitworks.com");
            message.setTo(toEmail);
            message.setSubject("【FitWorks】パスワード再設定のご案内");
            message.setText("以下のリンクをクリックしてパスワードを再設定してください。\n\n" + resetLink + "\n\n(このリンクは24時間有効です)");
            
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("メール送信に失敗しました: " + e.getMessage());
        }
    }
    
    public void save(User user) {
        userRepository.save(user);
    }
    
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return false;
        
        User user = optionalUser.get();
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    public void addExperience(String username, int xp) {
         Optional<User> optionalUser = userRepository.findByUsername(username);
         if (optionalUser.isPresent()) {
             User user = optionalUser.get();
             user.addXp(xp); 
             userRepository.save(user); 
         }
    }

    public void addExp(User user, int xp) {
        user.addXp(xp);
        userRepository.save(user);
    }
    
    public MissionStatusDto getDailyMissionStatus(User user) {
        final int MISSION_REWARD_XP = 300; 
        final String MISSION_TEXT = "筋トレ記録を1回投稿する";
        
        LocalDate today = LocalDate.now();
        
        if (user.getIsRewardClaimedToday() != null && user.getIsRewardClaimedToday() && 
            (user.getLastMissionCompletionDate() == null || !today.equals(user.getLastMissionCompletionDate()))) {
             user.setIsRewardClaimedToday(false);
             userRepository.save(user);
        }
        
        boolean isCompleted = (user.getLastMissionCompletionDate() != null && today.equals(user.getLastMissionCompletionDate()));

        return new MissionStatusDto(
            isCompleted,
            user.getIsRewardClaimedToday() != null ? user.getIsRewardClaimedToday() : false, 
            MISSION_REWARD_XP,
            MISSION_TEXT
        );
    }
    
    @Transactional
    public boolean claimMissionReward(User user) {
        MissionStatusDto status = getDailyMissionStatus(user);
        LocalDate today = LocalDate.now();

        if (status.isMissionCompleted() && !status.isRewardClaimed()) {
            user.addXp(status.getRewardXp());
            user.setLastMissionCompletionDate(today);
            user.setIsRewardClaimedToday(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public void incrementTrainingMissionProgress(User user) {
        LocalDate today = LocalDate.now();

        long todayRecordsCount = trainingRecordRepository.countByUser_IdAndRecordDate(user.getId(), today);
        if (todayRecordsCount > 0) {
            user.setLastMissionCompletionDate(today);
            if (user.getIsRewardClaimedToday() == null) {
                user.setIsRewardClaimedToday(false);
            }
            userRepository.save(user);
        }
    }

    @Transactional
    public void markMissionCompletedByPost(User user) {
        LocalDate today = LocalDate.now();
        user.setLastMissionCompletionDate(today);
        user.setIsRewardClaimedToday(false); 
        userRepository.save(user);
    }
 // ★追加: トークンからユーザーを取得（有効期限チェック付き）
    public User getByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token)
                .filter(u -> u.getTokenExpiration() != null && u.getTokenExpiration().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    // ★追加: パスワードを更新してトークンを無効化
    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null); // トークンをクリア
        user.setTokenExpiration(null);
        userRepository.save(user);
    }
}