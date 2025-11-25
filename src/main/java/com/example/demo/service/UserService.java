package com.example.demo.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.User;
// MissionStatusDto は MissionService の責務となるため、ここでは不要です。
// LocalDate はミッション進捗管理に不要となるため、ここでは不要です。
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TrainingRecordRepository trainingRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TrainingRecordRepository trainingRecordRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.trainingRecordRepository = trainingRecordRepository;
    }

    // ID検索 (MissionServiceなど外部サービスから利用されるため保持)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
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

    /**
     * ユーザーに経験値を付与し、レベルアップを処理します。
     * Userエンティティの addXp メソッドに処理を委譲します。
     * MissionServiceからミッション報酬として呼び出されます。
     *
     * @param user 対象ユーザー
     * @param expToAdd 付与する経験値量
     */
    @Transactional
    public void addExp(User user, int expToAdd) {
        // ★ 修正: Userエンティティの addXp メソッドで experiencePoints を更新するように統一
        user.addXp(expToAdd); 
        
        // 変更をDBに保存
        userRepository.save(user);
    }
}
