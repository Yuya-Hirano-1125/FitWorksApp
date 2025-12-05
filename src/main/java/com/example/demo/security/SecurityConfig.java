package com.example.demo.security;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.demo.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * CSRFトークンをレスポンスヘッダーにも載せるフィルター
     */
    private Filter csrfCookieFilter() {
        return (request, response, chain) -> {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (csrfToken != null) {
                httpResponse.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
            }

            try {
                chain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                e.printStackTrace();
            }
        };
    }

    @Bean
    @SuppressWarnings("deprecation")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // --- CORS ---
            .cors(Customizer.withDefaults())

            // --- CSRF（安全に Cookie 設定） ---
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .addFilterAfter(csrfCookieFilter(), BasicAuthenticationFilter.class)

            // --- アクセス制御 ---
            .authorizeHttpRequests(auth -> auth

                // 🔓 認証不要のパス
                .requestMatchers(
                    "/", "/login", "/register",
                    "/forgot-password", "/verify-code", "/reset-password",
                    "/error", "/terms",

                    // 静的ファイル
                    "/css/**", "/js/**", "/images/**", "/img/**"
                ).permitAll()

                // 🔒 必ずログインが必要なページ
                .requestMatchers(
                    "/home",
                    "/training", "/training/**",
                    "/settings", "/change-password",
                    "/community/**",
                    "/log/**",
                    "/characters/**",
                    "/daily-mission/**",
                    "/ranking/**",
                    "/ai-coach/**",
                    "/training-log/**"
                ).authenticated()

                // 🔒 ガチャは必ず“ログイン後のみ”
                .requestMatchers("/gacha/**").authenticated()

                // その他はすべて認証必要
                .anyRequest().authenticated()
            )

            // --- フォームログイン ---
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // --- ログアウト ---
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .permitAll()
            )

            // --- 認証プロバイダ ---
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
