package com.example.demo.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserService { 
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * ユーザー名でユーザーを検索し、Userエンティティを返す。（★追加★）
     * TrainingControllerから認証ユーザーを取得するために必要です。
     * @param username ユーザー名
     * @return Userエンティティ (見つからなかった場合はnull)
     */
    public User findByUsername(String username) {
        // UserRepositoryがOptional<User>を返すため、orElse(null)でUserエンティティを取り出す
        return userRepository.findByUsername(username).orElse(null); 
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return false;
        
        User user = optionalUser.get();
        // 現在のパスワードが一致するか検証
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            // パスワードをエンコードして更新
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
}