package com.example.demo.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.MissionStatusDto;
import com.example.demo.entity.User;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final TrainingRecordRepository trainingRecordRepository; 
    private final PasswordEncoder passwordEncoder; 

    // ★ 修正: TrainingRecordRepositoryとPasswordEncoderを含むコンストラクタ
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TrainingRecordRepository trainingRecordRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.trainingRecordRepository = trainingRecordRepository;
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
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
    
    // --- 経験値加算処理（既存の処理をそのまま残し、User.addXpを呼び出す）---
    public void addExperience(String username, int xp) {
         Optional<User> optionalUser = userRepository.findByUsername(username);
         if (optionalUser.isPresent()) {
             User user = optionalUser.get();
             user.addXp(xp); 
             userRepository.save(user); 
         }
    }
    
    // ★ 新規追加: デイリーミッションのステータスを取得するメソッド
    public MissionStatusDto getDailyMissionStatus(User user) {
        // ミッション定義
        final int MISSION_REWARD_XP = 500;
        final String MISSION_TEXT = "今日のトレーニングを1回記録する";
        
        LocalDate today = LocalDate.now();
        
        // 1. 報酬クレームのリセットロジック (日付が変わっていた場合)
        if (user.getIsRewardClaimedToday() != null && user.getIsRewardClaimedToday() && 
            (user.getLastMissionCompletionDate() == null || !today.equals(user.getLastMissionCompletionDate()))) {
             // 報酬クレーム済みだが、日付が変わっている場合はリセット
             user.setIsRewardClaimedToday(false);
             userRepository.save(user); // データベースにリセット状態を保存
        }
        
        // 2. トレーニング完了チェック (今日のトレーニング記録が1件以上あるか)
        long todayRecordsCount = trainingRecordRepository.countByUser_IdAndRecordDate(user.getId(), today);
        
        boolean isCompleted = todayRecordsCount > 0;

        return new MissionStatusDto(
            isCompleted,
            user.getIsRewardClaimedToday() != null ? user.getIsRewardClaimedToday() : false, 
            MISSION_REWARD_XP,
            MISSION_TEXT
        );
    }
    
    // ★ 新規追加: 経験値を付与し、ミッション報酬をクレーム済みにするメソッド
    public boolean claimMissionReward(User user) {
        MissionStatusDto status = getDailyMissionStatus(user);
        LocalDate today = LocalDate.now();

        if (status.isMissionCompleted() && !status.isRewardClaimed()) {
            // 経験値を付与 (この中でレベルアップも処理される)
            user.addXp(status.getRewardXp());
            
            // 報酬をクレーム済みにする
            user.setLastMissionCompletionDate(today);
            user.setIsRewardClaimedToday(true);
            
            userRepository.save(user);
            return true;
        }
        return false;
    }
}

