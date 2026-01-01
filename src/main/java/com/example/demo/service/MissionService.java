package com.example.demo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.DailyMissionStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.DailyMissionStatusRepository;
import com.example.demo.repository.UserRepository;

@Service
public class MissionService {

    // 報酬チップ量
    public static final int DAILY_MISSION_REWARD_CHIPS = 300;

    private final DailyMissionStatusRepository missionStatusRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    // ★ ミッション定義
    private record MissionDefinition(String type, String description, int requiredCount, int rewardExp) {}

    // ★ 全ミッションリスト
    private static final List<MissionDefinition> ALL_MISSIONS = List.of(
        new MissionDefinition("TRAINING_LOG", "トレーニングを1回記録する", 1, 1000),
        new MissionDefinition("COMMUNITY_POST", "コミュニティに1回投稿する", 1, 1000),
        new MissionDefinition("AI_COACH", "AIコーチを使ってみよう", 1, 1000),
        new MissionDefinition("MEAL_LOG", "食事を1回記録する", 1, 1000),         // 新規
        new MissionDefinition("WEIGHT_LOG", "体重を記録する", 1, 1000),           // 新規
        new MissionDefinition("GACHA_DRAW", "ガチャを1回引く", 1, 1000),          // 新規
        new MissionDefinition("CHECK_RANKING", "ランキングを確認する", 1, 1000)     // 新規
    );

    public MissionService(DailyMissionStatusRepository missionStatusRepository,
                          UserService userService,
                          UserRepository userRepository) {
        this.missionStatusRepository = missionStatusRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * 今日のミッションを取得（なければランダム生成）
     */
    @Transactional
    public List<DailyMissionStatus> getOrCreateTodayMissions(User user) {
        LocalDate today = LocalDate.now();
        List<DailyMissionStatus> missions = missionStatusRepository.findByUserAndDate(user, today);

        if (missions.isEmpty()) {
            // ★ 今日分がなければランダム生成
            missions = generateRandomDailyMissions(user, today);
        }
        return missions;
    }
    
    /**
     * ★追加: 達成済みかつ報酬未受け取りのミッションがあるか確認
     */
    @Transactional(readOnly = true)
    public boolean hasCompletedUnclaimedMissions(User user) {
        List<DailyMissionStatus> missions = getOrCreateTodayMissions(user);
        return missions.stream().anyMatch(m -> m.isCompleted() && !m.isRewardClaimed());
    }

    /**
     * ランダムに3つのミッションを生成
     */
    private List<DailyMissionStatus> generateRandomDailyMissions(User user, LocalDate date) {
        // リストをシャッフル
        List<MissionDefinition> candidates = new ArrayList<>(ALL_MISSIONS);
        Collections.shuffle(candidates);

        // 先頭3つを選択
        List<MissionDefinition> selected = candidates.subList(0, 3);
        
        List<DailyMissionStatus> newMissions = new ArrayList<>();
        for (MissionDefinition def : selected) {
            DailyMissionStatus mission = new DailyMissionStatus(
                user, date, def.type(), def.description(), def.requiredCount()
            );
            newMissions.add(mission);
        }

        missionStatusRepository.saveAll(newMissions);
        return newMissions;
    }

    // --- 進捗更新 ---
    @Transactional
    public void updateMissionProgress(Long userId, String missionType) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        LocalDate today = LocalDate.now();

        missionStatusRepository.findByUserAndDateAndMissionType(user, today, missionType)
                .ifPresent(missionStatus -> {
                    missionStatus.incrementProgress(); 
                    missionStatusRepository.save(missionStatus);
                });
    }

    // --- 報酬受け取り ---
    @Transactional
    public boolean claimMissionReward(Long userId, Long missionId) {
        Optional<DailyMissionStatus> optionalStatus = missionStatusRepository.findById(missionId);
        if (optionalStatus.isEmpty()) return false;

        DailyMissionStatus missionStatus = optionalStatus.get();

        if (!missionStatus.getUser().getId().equals(userId) || !missionStatus.getDate().equals(LocalDate.now())) {
            return false;
        }

        if (missionStatus.isCompleted() && !missionStatus.isRewardClaimed()) {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found."));
            
            int rewardExp = missionStatus.getRewardExp();

            // 経験値とチップを付与
            userService.addExp(user, rewardExp);
            user.addChips(DAILY_MISSION_REWARD_CHIPS);

            missionStatus.setRewardClaimed(true);

            missionStatusRepository.save(missionStatus);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // --- AM4:00 リセット ---
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Tokyo")
    @Transactional
    public void resetDailyMissions() {
        List<User> users = userRepository.findAll();
        LocalDate today = LocalDate.now();

        for (User user : users) {
            missionStatusRepository.deleteByUserAndDate(user, today);
            generateRandomDailyMissions(user, today);
        }
        System.out.println("✅ デイリーミッションを更新しました");
    }
}