package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private UserRepository userRepository;

    // ★ 1. トレーニングリマインダー (毎日 朝8時に実行)
    // @Scheduled(cron = "0 0 8 * * *") 
    public void sendTrainingReminders() {
        // リマインダーがONのユーザーを取得してメール送信処理を行う
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            if (user.isNotificationTrainingReminder()) {
                // TODO: メール送信ロジック (JavaMailSenderなどを使用)
                System.out.println("[Mock Email] トレーニングの時間ですよ！ -> " + user.getEmail());
            }
        }
    }

    // ★ 2. AIコーチの提案通知 (毎日 夕方18時に実行)
    // @Scheduled(cron = "0 0 18 * * *")
    public void sendAiSuggestions() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.isNotificationAiSuggestion()) {
                // TODO: AIサービスの提案生成ロジックを呼び出して送信
                System.out.println("[Mock Email] 今日のAIおすすめメニューが届きました -> " + user.getEmail());
            }
        }
    }

    // ★ 3. 進捗レポートメール (毎週日曜 20時に実行)
    // @Scheduled(cron = "0 0 20 * * SUN")
    public void sendWeeklyProgressReports() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.isNotificationProgressReport()) {
                // TODO: 今週の統計データを集計して送信
                System.out.println("[Mock Email] 今週のFitWorksレポートをお届けします -> " + user.getEmail());
            }
        }
    }
}