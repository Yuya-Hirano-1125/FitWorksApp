package com.example.demo.service;

import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private UserRepository userRepository;

    // 1. トレーニングリマインダー (毎日 朝8時に実行)
    // @Scheduled(cron = "0 0 8 * * *") 
    public void sendTrainingReminders() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.isNotificationTrainingReminder()) {
                // TODO: メール送信ロジック
                System.out.println("[Mock Email] トレーニングの時間ですよ！ -> " + user.getEmail());
            }
        }
    }

    // 2. AIコーチの提案通知 (毎日 夕方18時に実行)
    // @Scheduled(cron = "0 0 18 * * *")
    public void sendAiSuggestions() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.isNotificationAiSuggestion()) {
                System.out.println("[Mock Email] 今日のAIおすすめメニューが届きました -> " + user.getEmail());
            }
        }
    }

    // 3. 進捗レポートメール (毎週日曜 20時に実行)
    // @Scheduled(cron = "0 0 20 * * SUN")
    public void sendWeeklyProgressReports() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.isNotificationProgressReport()) {
                System.out.println("[Mock Email] 今週のFitWorksレポートをお届けします -> " + user.getEmail());
            }
        }
    }
    
    // ★追加: 生活リズムに合わせたリマインダー機能
    // 10分ごとに実行し、現在時刻がユーザーの設定時間と一致（または近い）場合に通知
    @Scheduled(cron = "0 0/10 * * * *") // 10分毎に実行
    public void sendLifestyleBasedReminders() {
        LocalTime now = LocalTime.now();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            // トレーニングリマインダーがONの人を対象
            if (user.isNotificationTrainingReminder()) {
                LocalTime userTime = user.getLifestyleReminderTime();
                
                // 現在時刻が設定時刻の前後5分以内であれば送信
                // (cronが10分おきなので、この範囲でヒットさせる)
                if (now.isAfter(userTime.minusMinutes(5)) && now.isBefore(userTime.plusMinutes(5))) {
                    
                    // 実際の通知処理 (メールやプッシュ通知)
                    String message = "【FitWorks】" + user.getUsername() + "さん、今なら3分運動できます！リフレッシュしましょう！";
                    System.out.println("[生活リズム通知] To: " + user.getEmail() + " Body: " + message);
                }
            }
        }
    }
}