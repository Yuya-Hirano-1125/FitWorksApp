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
    
    // アプリケーション起動時にテストユーザー (testuser/password) を登録する
	@Bean
	public CommandLineRunner run(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByUsername("testuser").isEmpty()) {
				User user = new User();
				user.setUsername("testuser");
				user.setPassword(passwordEncoder.encode("password"));
				userRepository.save(user);
				System.out.println("★テストユーザー 'testuser' を登録しました (初期パスワード: password)");
			}
		};
	}
}






















































