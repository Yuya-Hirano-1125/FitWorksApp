package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private SmsService smsService; // SMS送信用

    @Value("${app.base-url}")
    private String baseUrl;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TrainingRecordRepository trainingRecordRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.trainingRecordRepository = trainingRecordRepository;
    }
    
    // --- ユーザー登録処理 (電話番号対応) ---
    @Transactional
    public void registerNewUser(String username, String email, String phoneNumber, String rawPassword) {
        // 1. 重複チェック
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("そのユーザー名は既に使用されています。");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("そのメールアドレスは既に使用されています。");
        }
        if (phoneNumber != null && !phoneNumber.isEmpty() && userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("その電話番号は既に使用されています。");
        }

        // 2. ユーザー作成
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        
        newUser.setLevel(1);
        newUser.setExperiencePoints(0);
        newUser.setTheme("default");

        // 3. 保存
        userRepository.save(newUser);

        // 4. 登録完了メール送信 (任意)
        sendRegistrationEmail(newUser.getEmail(), newUser.getUsername());
    }

    // --- パスワードリセット (SMS認証版) ---
    @Transactional
    public boolean processForgotPasswordBySms(String phoneNumber) {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            // 6桁の数字コードを生成
            String code = String.format("%06d", new Random().nextInt(999999));
            
            // 認証コードを一時的に保存 (有効期限10分)
            user.setResetPasswordToken(code);
            user.setTokenExpiration(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);
            
            // SMS送信
            smsService.sendVerificationCode(user.getPhoneNumber(), code);
            
            return true;
        } else {
            System.out.println("電話番号が見つかりません: " + phoneNumber);
            return false;
        }
    }

    // --- パスワードリセット (メールリンク版) ---
    @Transactional
    public boolean processForgotPassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            String token = UUID.randomUUID().toString();
            user.setResetPasswordToken(token);
            user.setTokenExpiration(LocalDateTime.now().plusHours(24));
            userRepository.save(user);
            
            String resetLink = baseUrl + "/reset-password?token=" + token;
            sendResetEmail(user.getEmail(), resetLink);
            return true;
        }
        return false;
    }

    // --- 共通: トークン検証とパスワード更新 ---
    public User getByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token)
                .filter(u -> u.getTokenExpiration() != null && u.getTokenExpiration().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setTokenExpiration(null);
        userRepository.save(user);
    }

    // --- ★追加: フレンド機能関連 ---
    
    /**
     * ユーザー名でフレンドを追加する (相互フォローとして登録)
     */
    @Transactional
    public boolean addFriendByUsername(String currentUsername, String targetUsername) {
        if (currentUsername.equals(targetUsername)) {
            return false; // 自分自身は追加できない
        }
        
        Optional<User> currentUserOpt = userRepository.findByUsername(currentUsername);
        Optional<User> targetUserOpt = userRepository.findByUsername(targetUsername);

        if (currentUserOpt.isPresent() && targetUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            User targetUser = targetUserOpt.get();

            // 既にフレンドでないか確認などが必要であればここに追加
            // 今回はSetを使用しているため重複登録は自動で防がれる

            // 相互に追加
            currentUser.addFriend(targetUser);
            targetUser.addFriend(currentUser);

            userRepository.save(currentUser);
            userRepository.save(targetUser);
            return true;
        }
        return false;
    }

    /**
     * 自分とフレンドを含めたランキングリストを取得する
     */
    public List<User> getFriendRanking(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return new ArrayList<>();
        }
        User currentUser = userOpt.get();
        Set<User> friends = currentUser.getFriends();
        
        // 自分自身もリストに加える（表示用コピーを作成）
        List<User> rankingList = new ArrayList<>(friends);
        rankingList.add(currentUser);
        
        // 重複排除（念のため）
        rankingList = rankingList.stream().distinct().collect(Collectors.toList());

        // レベル降順 -> XP降順 でソート
        rankingList.sort(Comparator.comparingInt(User::getLevel).reversed()
                .thenComparing(Comparator.comparingInt(User::getXp).reversed()));
        
        return rankingList;
    }

    // --- ヘルパーメソッド (メール送信) ---
    private void sendRegistrationEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@fitworks.com");
            message.setTo(toEmail);
            message.setSubject("【FitWorks】登録完了のお知らせ");
            message.setText(username + "様、登録ありがとうございます。");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("メール送信失敗: " + e.getMessage());
        }
    }

    private void sendResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@fitworks.com");
            message.setTo(toEmail);
            message.setSubject("【FitWorks】パスワード再設定");
            message.setText("リンクをクリックして再設定してください:\n" + resetLink);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("メール送信失敗: " + e.getMessage());
        }
    }

    // --- 既存の参照用メソッド ---
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public void save(User user) {
        userRepository.save(user);
    }

    // --- その他のロジック (ミッション等) ---
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
         userRepository.findByUsername(username).ifPresent(user -> {
             user.addXp(xp); 
             userRepository.save(user); 
         });
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
    public void addExp(User user, int xp) {
        user.addXp(xp);
        userRepository.save(user);
    }
}