package com.example.demo.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
     * CORS設定
     * 信頼できるドメインからのみのリクエストを許可
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 開発中は localhost:3000 (React等) を許可
        configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:3000")); 
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Cookie送信を許可

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

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
                e.printStackTrace(); // 本番ではロガーを使用することを推奨
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // --- CORS設定の適用 ---
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // --- CSRF対策 ---
            .csrf(csrf -> csrf
                // JSからCookieを読み取れるようにする（SPA/Ajax用）
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .addFilterAfter(csrfCookieFilter(), BasicAuthenticationFilter.class)

            // --- セキュリティヘッダーの強化 (重要) ---
            .headers(headers -> headers
                // XSS対策: コンテンツセキュリティポリシー (CSP)
                // 許可されたソースからのみスクリプト等の読み込みを許可
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                    		"default-src 'self'; " + 
                                    "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://unpkg.com https://cdnjs.cloudflare.com; " + 
                                    "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com https://cdnjs.cloudflare.com https://unpkg.com; " + 
                                    "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; " +
                                    
                                    // ★修正: unpkg.com を追加（ピン画像用）
                                    // https://tile.openstreetmap.org (地図用) と https://unpkg.com (ピン画像用) の両方を許可
                                    "img-src 'self' data: https://tile.openstreetmap.org https://unpkg.com; " + 
                                    
                                    "connect-src 'self' https://overpass-api.de; " +
                                    "frame-ancestors 'self'"
                    )
                )
                // リファラーポリシー: プライバシー保護のため、外部サイトへの遷移時にURLパラメータ等を送らない
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                // HSTS: HTTPS強制 (本番環境でのみ有効化推奨、開発中はコメントアウトでも可)
                // .httpStrictTransportSecurity(hsts -> hsts
                //     .includeSubDomains(true)
                //     .maxAgeInSeconds(31536000)
                // )
            )

            // --- セッション管理 ---
            .sessionManagement(session -> session
                // ログイン時にセッションIDを変更し、セッション固定攻撃を防ぐ
                .sessionFixation().changeSessionId()
                // 同時ログイン数の制限
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )

            // --- アクセス制御 ---
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login", "/register",
                    "/forgot-password", "/verify-code", "/reset-password",
                    "/error", "/terms",
                    // APIエンドポイントを許可する場合はここに追加
                    "/api/public/**", 
                    "/css/**", "/js/**", "/images/**", "/img/**"
                ).permitAll()

                // Authenticated routes
                .requestMatchers(
                    "/home", "/training/**", "/settings/**",
                    "/community/**", "/log/**", "/characters/**",
                    "/daily-mission/**", "/ranking/**", "/ai-coach/**",
                    "/training-log/**",
                    "/api/**" // APIへのアクセスも認証必須にする
                ).authenticated()

                .requestMatchers("/gacha/**").authenticated()
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
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST")) // POSTメソッドを強制
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .permitAll()
            )

            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}