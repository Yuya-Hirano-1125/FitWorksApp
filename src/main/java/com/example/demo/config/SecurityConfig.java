package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 開発中はCSRF無効化
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() // ← APIは全て認証不要
                .anyRequest().authenticated() // それ以外は認証必要
            )
            .formLogin(form -> form.permitAll()) // ログインフォーム許可
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}