package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class LevelService {

    private final UserRepository userRepository;

    public LevelService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザーにXPを加算し、必要ならレベルアップさせて保存する
     */
    public void addXpAndCheckLevelUp(User user, int earnedXp) {
        user.addXp(earnedXp);         // User.java に定義されたXP加算＆レベルアップ処理
        userRepository.save(user);    // DBに保存
    }
}
