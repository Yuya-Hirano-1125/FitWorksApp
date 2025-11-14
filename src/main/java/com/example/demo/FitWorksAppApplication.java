package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching; // ★ 既存
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync; // ★ 追加
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@SpringBootApplication
@EnableCaching // ★ キャッシュ機能の有効化
@EnableAsync // ★ 非同期処理機能の有効化
public class FitWorksAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitWorksAppApplication.class, args);
	}

    /**
     * アプリケーション起動時にテストユーザー (testuser/password) を登録する。
     * @param userRepository ユーザーリポジトリ
     * @param passwordEncoder パスワードエンコーダー
     * @return CommandLineRunner
     */
	@Bean
	public CommandLineRunner run(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// ユーザーが存在しない場合のみ登録
			if (userRepository.findByUsername("test").isEmpty()) {
				User user = new User();
				user.setUsername("");
				// パスワードをハッシュ化して保存
				user.setPassword(passwordEncoder.encode("test"));
				userRepository.save(user);
				System.out.println("★テストユーザー 'testuser' を登録しました (初期パスワード: password)");
			}
		};
	}
}












































