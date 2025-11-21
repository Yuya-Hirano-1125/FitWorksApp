package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@SpringBootApplication
public class FitWorksAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitWorksAppApplication.class, args);
    }

    /**
     * アプリケーション起動時にデフォルトユーザーを作成するCommandLineRunner
     */
    @Bean
    public CommandLineRunner dataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // --- 1. ユーザー 'test' の作成 ---
            final String USERNAME_TEST = "test";
            if (userRepository.findByUsername(USERNAME_TEST).isEmpty()) {
                
                String encodedPassword = passwordEncoder.encode(USERNAME_TEST);
                
                User defaultUser = new User();
                defaultUser.setUsername(USERNAME_TEST);
                defaultUser.setPassword(encodedPassword);
                defaultUser.setEmail("test@fitworks.com"); 
                
                userRepository.save(defaultUser);
                
                System.out.println("✅ Default user '" + USERNAME_TEST + "' created successfully. (Password: test)");
            }

            // --- 2. ユーザー 'user2' の追加 (★ 新規追加ブロック ★) ---
            final String USERNAME_USER2 = "user";
            if (userRepository.findByUsername(USERNAME_USER2).isEmpty()) {
                
                // パスワード 'user2' をBCryptでエンコード
                String encodedPassword = passwordEncoder.encode(USERNAME_USER2); 
                
                User secondUser = new User();
                secondUser.setUsername(USERNAME_USER2);
                secondUser.setPassword(encodedPassword);
                secondUser.setEmail("user@fitworks.com"); // 仮のメールアドレス
                
                userRepository.save(secondUser);
                
                System.out.println("✅ Second default user '" + USERNAME_USER2 + "' created successfully. (Password: user)");
            }
        };
    }
}