package com.example.demo.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.User;
import com.example.demo.entity.UserMuscle;
import com.example.demo.repository.UserMuscleRepository;

@Service
public class MuscleService {

    private final UserMuscleRepository userMuscleRepository;
    
    // 管理対象とする部位リスト
    private static final List<String> TARGET_PARTS = Arrays.asList("胸", "背中", "脚", "肩", "腕", "腹筋", "その他", "有酸素");

    public MuscleService(UserMuscleRepository userMuscleRepository) {
        this.userMuscleRepository = userMuscleRepository;
    }

    // ユーザーの全筋肉を取得（なければ初期生成）
    @Transactional
    public List<UserMuscle> getUserMuscles(User user) {
        List<UserMuscle> existingMuscles = userMuscleRepository.findByUser(user);
        
        // 足りない部位があれば作成する
        boolean added = false;
        for (String part : TARGET_PARTS) {
            if (existingMuscles.stream().noneMatch(m -> m.getTargetPart().equals(part))) {
                UserMuscle newMuscle = new UserMuscle(user, part, "マイ" + part); // デフォルト名
                userMuscleRepository.save(newMuscle);
                existingMuscles.add(newMuscle);
                added = true;
            }
        }
        if (added) {
            // 再取得してリストを更新
            return userMuscleRepository.findByUser(user);
        }
        return existingMuscles;
    }

    @Transactional
    public void updateMuscleName(Long muscleId, User user, String newName) {
        Optional<UserMuscle> opt = userMuscleRepository.findById(muscleId);
        if (opt.isPresent() && opt.get().getUser().getId().equals(user.getId())) {
            UserMuscle muscle = opt.get();
            muscle.setCustomName(newName);
            userMuscleRepository.save(muscle);
        }
    }

    // 経験値を加算する
    @Transactional
    public String addExperience(User user, String part, int xp) {
        if (!TARGET_PARTS.contains(part)) {
            part = "その他";
        }

        // 初期化も含めて取得
        getUserMuscles(user); 
        
        Optional<UserMuscle> opt = userMuscleRepository.findByUserAndTargetPart(user, part);
        if (opt.isPresent()) {
            UserMuscle muscle = opt.get();
            boolean leveledUp = muscle.addXp(xp);
            userMuscleRepository.save(muscle);
            
            if (leveledUp) {
                return muscle.getCustomName() + " (部位: " + part + ") のレベルが " + muscle.getLevel() + " に上がりました！";
            }
        }
        return null; // レベルアップなし
    }
}