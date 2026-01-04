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
                System.out.println("✅ Default user '" + USERNAME_TEST + "' created successfully.");
            }

            // --- 2. ユーザー 'user' の作成 ---
            final String USERNAME_USER2 = "user";
            if (userRepository.findByUsername(USERNAME_USER2).isEmpty()) {
                String encodedPassword = passwordEncoder.encode(USERNAME_USER2);
                User secondUser = new User();
                secondUser.setUsername(USERNAME_USER2);
                secondUser.setPassword(encodedPassword);
                secondUser.setEmail("user@fitworks.com");
                userRepository.save(secondUser);
                System.out.println("✅ User '" + USERNAME_USER2 + "' created successfully.");
            }

            // --- ★ 3. 管理者 'admin' (最強アカウント) の作成 ★ ---
            final String USERNAME_ADMIN = "admin";
            if (userRepository.findByUsername(USERNAME_ADMIN).isEmpty()) {
                // パスワード "admin" をハッシュ化
                String encodedPassword = passwordEncoder.encode("admin"); 
                
                User adminUser = new User();
                adminUser.setUsername(USERNAME_ADMIN);
                adminUser.setPassword(encodedPassword);
                adminUser.setEmail("admin@example.com");
                
                // ★ 日付もJavaならこれでエラーになりません
                adminUser.setBirthDate(java.time.LocalDate.of(2000, 1, 1)); 
                
                // ★ 最強ステータス設定
                adminUser.setLevel(999);
                adminUser.setExperiencePoints(9999999);
                adminUser.setChipCount(9999999);
                
                userRepository.save(adminUser);
                System.out.println("✅ Admin user '" + USERNAME_ADMIN + "' created successfully. (Pass: admin)");
            }
        };
    }
}