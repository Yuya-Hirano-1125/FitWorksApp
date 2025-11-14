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
     * ユーザー名: test, パスワード: test (BCryptでハッシュ化)
     */
    @Bean
    public CommandLineRunner dataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // ユーザー名 'test' が存在するかチェック
            if (userRepository.findByUsername("test").isEmpty()) {
                
                // パスワード 'test' をBCryptでエンコード
                String encodedPassword = passwordEncoder.encode("test");
                
                // 新しいユーザーを作成
                User defaultUser = new User();
                defaultUser.setUsername("test");
                defaultUser.setPassword(encodedPassword);
                defaultUser.setEmail("test@fitworks.com"); // 仮のメールアドレス
                
                // データベースに保存
                userRepository.save(defaultUser);
                
                System.out.println("✅ Default user 'test' created successfully. (Password: test)");
            }
        };
    }
}




