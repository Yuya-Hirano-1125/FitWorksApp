package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.DailyMissionStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.DailyMissionStatusRepository;
import com.example.demo.repository.UserRepository;

@Service
public class MissionService {

    private final DailyMissionStatusRepository missionStatusRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public MissionService(DailyMissionStatusRepository missionStatusRepository,
                          UserService userService,
                          UserRepository userRepository) {
        this.missionStatusRepository = missionStatusRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * ユーザーの今日のデイリーミッションを取得します。
     * ミッションが不足している場合は補完して必ず3件返します。
     */
    @Transactional
    public List<DailyMissionStatus> getOrCreateTodayMissions(User user) {
        LocalDate today = LocalDate.now();
        List<DailyMissionStatus> missions = missionStatusRepository.findByUserAndDate(user, today);

        if (missions.isEmpty()) {
            // ★ 初回は必ず3件生成
            missions = generateDailyMissions(user, today);
        } else {
            // ミッションタイプの一覧（順番固定）
            List<String> expectedTypes = List.of("TRAINING_LOG", "COMMUNITY_POST", "AI_COACH");

            List<String> existingTypes = missions.stream()
                    .map(DailyMissionStatus::getMissionType)
                    .toList();

            List<String> missingTypes = expectedTypes.stream()
                    .filter(type -> !existingTypes.contains(type))
                    .toList();

            for (String type : missingTypes) {
                DailyMissionStatus newMission = switch (type) {
                    case "TRAINING_LOG" -> new DailyMissionStatus(user, today, type, "トレーニングを1回記録する", 1, 300);
                    case "COMMUNITY_POST" -> new DailyMissionStatus(user, today, type, "コミュニティに1回投稿する", 1, 200);
                    case "AI_COACH" -> new DailyMissionStatus(user, today, type, "AIコーチを使ってみよう", 1, 150);
                    default -> throw new IllegalArgumentException("Unknown mission type: " + type);
                };
                missionStatusRepository.save(newMission);
                missions.add(newMission);
            }
        }

        return missions;
    }

    /**
     * デイリーミッションを生成します（初回用）。
     */
    private List<DailyMissionStatus> generateDailyMissions(User user, LocalDate date) {
        DailyMissionStatus mission1 = new DailyMissionStatus(
            user, date, "TRAINING_LOG", "トレーニングを1回記録する", 1, 300
        );
        DailyMissionStatus mission2 = new DailyMissionStatus(
            user, date, "COMMUNITY_POST", "コミュニティに1回投稿する", 1, 200
        );
        DailyMissionStatus mission3 = new DailyMissionStatus(
            user, date, "AI_COACH", "AIコーチを使ってみよう", 1, 150
        );

        missionStatusRepository.saveAll(List.of(mission1, mission2, mission3));
        return List.of(mission1, mission2, mission3);
    }

    /**
     * ミッションの進捗を更新します。
     */
    @Transactional
    public void updateMissionProgress(Long userId, String missionType) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        LocalDate today = LocalDate.now();

        missionStatusRepository.findByUserAndDateAndMissionType(user, today, missionType)
                .ifPresent(missionStatus -> {
                    missionStatus.incrementProgress(); // currentCount++ & completed 判定
                    missionStatusRepository.save(missionStatus);
                });
    }

    /**
     * 完了したミッションの報酬を獲得します (経験値を付与します)。
     */
    @Transactional
    public boolean claimMissionReward(Long userId, Long missionId) {
        Optional<DailyMissionStatus> optionalStatus = missionStatusRepository.findById(missionId);
        if (optionalStatus.isEmpty()) return false;

        DailyMissionStatus missionStatus = optionalStatus.get();

        // ユーザーと今日のミッションであることを確認
        if (!missionStatus.getUser().getId().equals(userId) || !missionStatus.getDate().equals(LocalDate.now())) {
            return false;
        }

        // ミッションが完了しており、まだ報酬未受け取りなら処理
        if (missionStatus.isCompleted() && !missionStatus.isRewardClaimed()) {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found in claim process."));
            int rewardExp = missionStatus.getRewardExp();

            // 経験値付与
            userService.addExp(user, rewardExp);

            // 報酬受け取り済みに更新
            missionStatus.setRewardClaimed(true);

            // 保存
            missionStatusRepository.save(missionStatus);
            userRepository.save(user);

            return true;
        }
        return false;
    }
}
