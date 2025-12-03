package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime; // 追加
import java.util.Optional;
import java.util.UUID; // 追加

import org.springframework.beans.factory.annotation.Autowired; // 追加
import org.springframework.beans.factory.annotation.Value; // 追加
import org.springframework.mail.SimpleMailMessage; // 追加
import org.springframework.mail.javamail.JavaMailSender; // 追加
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

    // ★追加: メールセンダー
    @Autowired
    private JavaMailSender mailSender;

    // ★追加: ベースURL (application.propertiesから取得)
    @Value("${app.base-url}")
    private String baseUrl;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TrainingRecordRepository trainingRecordRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.trainingRecordRepository = trainingRecordRepository;
    }
    
    // --- 新規追加: ユーザー登録処理 ---
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
        // パスワードをBCryptでハッシュ化して保存
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        
        // 初期設定
        newUser.setLevel(1);
        newUser.setExperiencePoints(0);
        newUser.setTheme("default");

        // 4. DBに保存
        userRepository.save(newUser);
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
    
    // ★修正: パスワードリセット処理（メール送信実装）
    @Transactional
    public boolean processForgotPassword(String email) {
        Optional<User> optionalUser = findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            // 1. トークンの生成 (UUID)
            String token = UUID.randomUUID().toString();
            
            // 2. ユーザー情報にトークンと有効期限(例: 24時間)を保存
            user.setResetPasswordToken(token);
            user.setTokenExpiration(LocalDateTime.now().plusHours(24));
            userRepository.save(user); // DB更新
            
            // 3. リセットリンクの作成
            String resetLink = baseUrl + "/reset-password?token=" + token;
            
            // 4. メール送信
            sendResetEmail(user.getEmail(), resetLink);
            
            System.out.println("パスワードリセットリンクを送信しました: " + email);
            return true;
        } else {
            System.out.println("メールアドレスが見つかりません: " + email);
            return false;
        }
    }

    // ★追加: メール送信のヘルパーメソッド
    private void sendResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@fitworks.com"); // 送信元(設定によっては上書きされる場合あり)
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
}