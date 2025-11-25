package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.DailyMissionStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.DailyMissionStatusRepository;

@Service
public class MissionService {

    private final DailyMissionStatusRepository missionStatusRepository;
    private final UserService userService; 

    public MissionService(DailyMissionStatusRepository missionStatusRepository, UserService userService) {
        this.missionStatusRepository = missionStatusRepository;
        this.userService = userService;
    }

    /**
     * ユーザーの今日のデイリーミッションを取得します。
     * ミッションがない場合は新しく生成します (進捗を一から始める)。
     */
    @Transactional
    public List<DailyMissionStatus> getOrCreateTodayMissions(User user) {
        LocalDate today = LocalDate.now();
        List<DailyMissionStatus> missions = missionStatusRepository.findByUserAndDate(user, today);

        if (missions.isEmpty()) {
            return generateDailyMissions(user, today);
        }

        return missions;
    }

    /**
     * デイリーミッションを生成します。
     * ★ 報酬を一律1000XPに変更
     */
    private List<DailyMissionStatus> generateDailyMissions(User user, LocalDate date) {
        DailyMissionStatus mission1 = new DailyMissionStatus(
            user, date, "TRAINING_LOG", "トレーニングを1回記録する", 1, 1000
        );
        DailyMissionStatus mission2 = new DailyMissionStatus(
            user, date, "COMMUNITY_POST", "コミュニティに1回投稿する", 1, 1000
        );

        missionStatusRepository.saveAll(List.of(mission1, mission2));

        return List.of(mission1, mission2);
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
                    missionStatus.incrementProgress();
                    missionStatusRepository.save(missionStatus);
                });
    }

    /**
     * 完了したミッションの報酬を獲得します (経験値を付与します)。
     * ★ 報酬を一律1000XPに変更
     */
    @Transactional
    public boolean claimMissionReward(Long userId, Long missionId) {
        Optional<DailyMissionStatus> optionalStatus = missionStatusRepository.findById(missionId);

        if (optionalStatus.isEmpty()) return false;
        DailyMissionStatus missionStatus = optionalStatus.get();
        
        // ユーザーと今日のミッションであることの確認
        if (!missionStatus.getUser().getId().equals(userId) || !missionStatus.getDate().equals(LocalDate.now())) {
             return false;
        }

        // ミッションが完了しており、まだ報酬を獲得していないこと
        if (missionStatus.isCompleted() && !missionStatus.isRewardClaimed()) {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found in claim process."));

            // ★ 一律1000XPを付与
            userService.addExp(user, 1000);

            // 報酬をクレーム済みに更新
            missionStatus.setRewardClaimed(true);
            missionStatusRepository.save(missionStatus);
            
            return true;
        }
        
        return false;
    }
}
