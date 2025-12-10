package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.AppTitle;
import com.example.demo.entity.User;
import com.example.demo.entity.UserTitle;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserTitleRepository;

@Service
public class TitleService {

    private final UserTitleRepository userTitleRepository;
    private final UserRepository userRepository;
    private final TrainingRecordRepository trainingRecordRepository;

    public TitleService(UserTitleRepository userTitleRepository, UserRepository userRepository, TrainingRecordRepository trainingRecordRepository) {
        this.userTitleRepository = userTitleRepository;
        this.userRepository = userRepository;
        this.trainingRecordRepository = trainingRecordRepository;
    }

    // ユーザーが持っている称号リストを取得
    public List<AppTitle> getUnlockedTitles(User user) {
        return userTitleRepository.findByUser(user).stream()
                .map(UserTitle::getTitle)
                .collect(Collectors.toList());
    }

    // 称号を装備する
    @Transactional
    public void equipTitle(User user, AppTitle title) {
        // 持っていない称号は装備できない
        if (title != null && !userTitleRepository.existsByUserAndTitle(user, title)) {
            throw new IllegalArgumentException("まだ獲得していない称号です。");
        }
        user.setEquippedTitle(title);
        userRepository.save(user);
    }

    // --- 条件チェック & 解放ロジック ---
    @Transactional
    public void checkAndUnlockTitles(User user) {
        // 1. 初回ログイン (BEGINNER) - 無条件で付与
        unlockIfConditionMet(user, AppTitle.BEGINNER, true);

        // 2. レベル条件
        int level = user.getLevel();
        unlockIfConditionMet(user, AppTitle.ROOKIE_TRAINER, level >= 5);
        unlockIfConditionMet(user, AppTitle.INTERMEDIATE, level >= 10);
        unlockIfConditionMet(user, AppTitle.VETERAN, level >= 30);
        unlockIfConditionMet(user, AppTitle.ELITE, level >= 50);
        unlockIfConditionMet(user, AppTitle.MUSCLE_GOD, level >= 100);

        // 3. トレーニング記録数
        long totalRecords = trainingRecordRepository.countByUser_Id(user.getId());
        unlockIfConditionMet(user, AppTitle.FIRST_STEP, totalRecords >= 1);
        unlockIfConditionMet(user, AppTitle.DILIGENT, totalRecords >= 50);
        unlockIfConditionMet(user, AppTitle.IRON_MAN, totalRecords >= 100);
        
        // 4. 部位別マスタリー
        unlockIfConditionMet(user, AppTitle.CHEST_LOVER, trainingRecordRepository.countByUserAndBodyPartContaining(user, "胸") >= 10);
        unlockIfConditionMet(user, AppTitle.BACK_DEMON, trainingRecordRepository.countByUserAndBodyPartContaining(user, "背中") >= 10);
        unlockIfConditionMet(user, AppTitle.LEG_DAY_SURVIVOR, trainingRecordRepository.countByUserAndBodyPartContaining(user, "脚") >= 10);
        unlockIfConditionMet(user, AppTitle.SHOULDER_KING, trainingRecordRepository.countByUserAndBodyPartContaining(user, "肩") >= 10);
        unlockIfConditionMet(user, AppTitle.ARM_WRESTLER, trainingRecordRepository.countByUserAndBodyPartContaining(user, "腕") >= 10);
        unlockIfConditionMet(user, AppTitle.ABS_OF_STEEL, trainingRecordRepository.countByUserAndBodyPartContaining(user, "腹筋") >= 10);
    }

    private void unlockIfConditionMet(User user, AppTitle title, boolean condition) {
        if (condition) {
            if (!userTitleRepository.existsByUserAndTitle(user, title)) {
                UserTitle newTitle = new UserTitle(user, title);
                userTitleRepository.save(newTitle);
            }
        }
    }
}