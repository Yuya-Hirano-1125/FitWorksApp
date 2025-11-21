package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.DailyMissionStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.DailyMissionStatusRepository;

@Service
public class MissionService {

    private final DailyMissionStatusRepository missionStatusRepository;
    private final UserService userService; // EXP付与のためにUserServiceを使用

    public MissionService(DailyMissionStatusRepository missionStatusRepository, UserService userService) {
        this.missionStatusRepository = missionStatusRepository;
        this.userService = userService;
    }

    /**
     * ユーザーの今日のデイリーミッションを取得します。
     * ミッションがない場合は新しく生成します。
     *
     * @param user 対象ユーザー
     * @return 今日のミッションリスト
     */
    public List<DailyMissionStatus> getOrCreateTodayMissions(User user) {
        LocalDate today = LocalDate.now();
        List<DailyMissionStatus> missions = missionStatusRepository.findByUserAndDate(user, today);

        // ミッション進捗を一から始める (日付チェック)
        if (missions.isEmpty()) {
            // 今日のミッションがないため、新しく生成
            return generateDailyMissions(user, today);
        }

        return missions;
    }

    /**
     * デイリーミッションを生成します。
     * この部分は、ミッションの内容を定義するロジックに置き換えてください。
     */
    private List<DailyMissionStatus> generateDailyMissions(User user, LocalDate date) {
        // 例: 運動記録を1回行うミッション
        DailyMissionStatus mission1 = new DailyMissionStatus(user, date, "TRAINING_LOG", 1, 150);
        // 例: コミュニティに1回投稿するミッション
        DailyMissionStatus mission2 = new DailyMissionStatus(user, date, "COMMUNITY_POST", 1, 100);

        missionStatusRepository.save(mission1);
        missionStatusRepository.save(mission2);

        return List.of(mission1, mission2);
    }

    /**
     * ミッションの進捗を更新し、完了していれば経験値を付与します。
     *
     * @param userId ユーザーID
     * @param missionType 更新対象のミッションタイプ (例: "TRAINING_LOG")
     */
    @Transactional
    public void updateMissionProgressAndCheckCompletion(Long userId, String missionType) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        LocalDate today = LocalDate.now();
        
        missionStatusRepository.findByUserAndDateAndMissionType(user, today, missionType)
                .ifPresent(missionStatus -> {
                    boolean completedNow = missionStatus.incrementProgress();
                    
                    // ミッションの進捗を保存
                    missionStatusRepository.save(missionStatus);
                    
                    // ミッションクリアで経験値を獲得
                    if (completedNow) {
                        userService.addExp(user, missionStatus.getRewardExp());
                    }
                });
    }

    /**
     * MissionControllerなどに返すためのDto変換メソッド（任意）
     */
    public List<DailyMissionStatus> getTodayMissions(User user) {
        return getOrCreateTodayMissions(user);
    }
}